package seraph.zion.signal.jobs;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.android.mms.dom.smil.parser.SmilXmlSerializer;
import com.annimon.stream.Stream;
import com.google.android.mms.ContentType;
import com.google.android.mms.InvalidHeaderValueException;
import com.google.android.mms.pdu_alt.CharacterSets;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.PduBody;
import com.google.android.mms.pdu_alt.PduComposer;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduPart;
import com.google.android.mms.pdu_alt.SendConf;
import com.google.android.mms.pdu_alt.SendReq;
import com.google.android.mms.smil.SmilHelper;
import com.klinker.android.send_message.Utils;

import org.signal.core.util.Hex;
import org.signal.core.util.StreamUtil;
import org.signal.core.util.logging.Log;
import seraph.zion.signal.attachments.Attachment;
import seraph.zion.signal.attachments.DatabaseAttachment;
import seraph.zion.signal.database.GroupTable;
import seraph.zion.signal.database.MessageTable;
import seraph.zion.signal.database.NoSuchMessageException;
import seraph.zion.signal.database.SignalDatabase;
import seraph.zion.signal.database.ThreadTable;
import seraph.zion.signal.dependencies.ApplicationDependencies;
import seraph.zion.signal.jobmanager.JsonJobData;
import seraph.zion.signal.jobmanager.Job;
import seraph.zion.signal.jobmanager.JobLogger;
import seraph.zion.signal.jobmanager.JobManager;
import seraph.zion.signal.jobmanager.impl.NetworkConstraint;
import seraph.zion.signal.keyvalue.SignalStore;
import seraph.zion.signal.mms.CompatMmsConnection;
import seraph.zion.signal.mms.MediaConstraints;
import seraph.zion.signal.mms.MmsException;
import seraph.zion.signal.mms.MmsSendResult;
import seraph.zion.signal.mms.OutgoingMessage;
import seraph.zion.signal.mms.PartAuthority;
import seraph.zion.signal.notifications.v2.ConversationId;
import seraph.zion.signal.phonenumbers.NumberUtil;
import seraph.zion.signal.recipients.Recipient;
import seraph.zion.signal.transport.InsecureFallbackApprovalException;
import seraph.zion.signal.transport.UndeliverableMessageException;
import seraph.zion.signal.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

public final class MmsSendJob extends SendJob {

  public static final String KEY = "MmsSendJobV2";

  private static final String TAG = Log.tag(MmsSendJob.class);

  private static final String KEY_MESSAGE_ID = "message_id";

  private final long messageId;

  private MmsSendJob(long messageId) {
    this(new Job.Parameters.Builder()
                           .setQueue("mms-operation")
                           .addConstraint(NetworkConstraint.KEY)
                           .setMaxAttempts(15)
                           .build(),
         messageId);
  }

  /** Enqueues compression jobs for attachments and finally the MMS send job. */
  @WorkerThread
  public static void enqueue(@NonNull Context context, @NonNull JobManager jobManager, long messageId) {
    MessageTable    database = SignalDatabase.messages();
    OutgoingMessage message;

    try {
      message = database.getOutgoingMessage(messageId);
    } catch (MmsException | NoSuchMessageException e) {
      throw new AssertionError(e);
    }

    List<Job> compressionJobs = Stream.of(message.getAttachments())
                                      .map(a -> (Job) AttachmentCompressionJob.fromAttachment((DatabaseAttachment) a, true, -1))
                                      .toList();

    MmsSendJob sendJob = new MmsSendJob(messageId);

    jobManager.startChain(compressionJobs)
              .then(sendJob)
              .enqueue();
  }

  private MmsSendJob(@NonNull Job.Parameters parameters, long messageId) {
    super(parameters);
    this.messageId = messageId;
  }

  @Override
  public @Nullable byte[] serialize() {
    return new JsonJobData.Builder().putLong(KEY_MESSAGE_ID, messageId).serialize();
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void onAdded() {
    SignalDatabase.messages().markAsSending(messageId);
  }

  @Override
  public void onSend() throws MmsException, NoSuchMessageException, IOException {
    MessageTable    database = SignalDatabase.messages();
    OutgoingMessage message  = database.getOutgoingMessage(messageId);

    if (database.isSent(messageId)) {
      Log.w(TAG, "Message " + messageId + " was already sent. Ignoring.");
      return;
    }

    try {
      Log.i(TAG, "Sending message: " + messageId);

      SendReq pdu = constructSendPdu(message);

      validateDestinations(message, pdu);

      final byte[]        pduBytes = getPduBytes(pdu);
      final SendConf      sendConf = new CompatMmsConnection(context).send(pduBytes, -1);
      final MmsSendResult result   = getSendResult(sendConf, pdu);

      database.markAsSent(messageId, false);
      markAttachmentsUploaded(messageId, message);

      Log.i(TAG, "Sent message: " + messageId);
    } catch (UndeliverableMessageException | IOException e) {
      Log.w(TAG, e);
      database.markAsSentFailed(messageId);
      notifyMediaMessageDeliveryFailed(context, messageId);
    } catch (InsecureFallbackApprovalException e) {
      Log.w(TAG, e);
      database.markAsPendingInsecureSmsFallback(messageId);
      notifyMediaMessageDeliveryFailed(context, messageId);
    }
  }

  @Override
  public boolean onShouldRetry(@NonNull Exception exception) {
    return false;
  }

  @Override
  public void onFailure() {
    Log.i(TAG, JobLogger.format(this, "onFailure() messageId: " + messageId));
    SignalDatabase.messages().markAsSentFailed(messageId);
    notifyMediaMessageDeliveryFailed(context, messageId);
  }

  private byte[] getPduBytes(SendReq message)
      throws IOException, UndeliverableMessageException, InsecureFallbackApprovalException
  {
    byte[] pduBytes = new PduComposer(context, message).make();

    if (pduBytes == null) {
      throw new UndeliverableMessageException("PDU composition failed, null payload");
    }

    return pduBytes;
  }

  private MmsSendResult getSendResult(SendConf conf, SendReq message)
      throws UndeliverableMessageException
  {
    if (conf == null) {
      throw new UndeliverableMessageException("No M-Send.conf received in response to send.");
    } else if (conf.getResponseStatus() != PduHeaders.RESPONSE_STATUS_OK) {
      throw new UndeliverableMessageException("Got bad response: " + conf.getResponseStatus());
    } else if (isInconsistentResponse(message, conf)) {
      throw new UndeliverableMessageException("Mismatched response!");
    } else {
      return new MmsSendResult(conf.getMessageId(), conf.getResponseStatus());
    }
  }

  private boolean isInconsistentResponse(SendReq message, SendConf response) {
    Log.i(TAG, "Comparing: " + Hex.toString(message.getTransactionId()));
    Log.i(TAG, "With:      " + Hex.toString(response.getTransactionId()));
    return !Arrays.equals(message.getTransactionId(), response.getTransactionId());
  }

  private void validateDestinations(EncodedStringValue[] destinations) throws UndeliverableMessageException {
    if (destinations == null) return;

    for (EncodedStringValue destination : destinations) {
      if (destination == null || !NumberUtil.isValidSmsOrEmail(destination.getString())) {
        throw new UndeliverableMessageException("Invalid destination: " +
                                                (destination == null ? null : destination.getString()));
      }
    }
  }

  private void validateDestinations(OutgoingMessage media, SendReq message) throws UndeliverableMessageException {
    validateDestinations(message.getTo());
    validateDestinations(message.getCc());
    validateDestinations(message.getBcc());

    if (message.getTo() == null && message.getCc() == null && message.getBcc() == null) {
      throw new UndeliverableMessageException("No to, cc, or bcc specified!");
    }

    if (media.isSecure()) {
      throw new UndeliverableMessageException("Attempt to send encrypted MMS?");
    }
  }

  private SendReq constructSendPdu(OutgoingMessage message)
      throws UndeliverableMessageException
  {
    SendReq          req               = new SendReq();
    String           lineNumber        = getMyNumber(context);
    MediaConstraints mediaConstraints  = MediaConstraints.getMmsMediaConstraints(-1);
    List<Attachment> scaledAttachments = message.getAttachments();

    if (!TextUtils.isEmpty(lineNumber)) {
      req.setFrom(new EncodedStringValue(lineNumber));
    } else {
      req.setFrom(new EncodedStringValue(SignalStore.account().getE164()));
    }

    if (message.getThreadRecipient().isMmsGroup()) {
      List<Recipient> members = SignalDatabase.groups().getGroupMembers(message.getThreadRecipient().requireGroupId(), GroupTable.MemberSet.FULL_MEMBERS_EXCLUDING_SELF);

      for (Recipient member : members) {
        if (!member.hasSmsAddress()) {
          throw new UndeliverableMessageException("One of the group recipients did not have an SMS address! " + member.getId());
        }

        if (message.getDistributionType() == ThreadTable.DistributionTypes.BROADCAST) {
          req.addBcc(new EncodedStringValue(member.requireSmsAddress()));
        } else {
          req.addTo(new EncodedStringValue(member.requireSmsAddress()));
        }
      }
    } else {
      if (!message.getThreadRecipient().hasSmsAddress()) {
        throw new UndeliverableMessageException("Recipient did not have an SMS address! " + message.getThreadRecipient().getId());
      }

      req.addTo(new EncodedStringValue(message.getThreadRecipient().requireSmsAddress()));
    }

    req.setDate(System.currentTimeMillis() / 1000);

    PduBody body = new PduBody();
    int     size = 0;

    if (!TextUtils.isEmpty(message.getBody())) {
      PduPart part = new PduPart();
      String name = String.valueOf(System.currentTimeMillis());
      part.setData(Util.toUtf8Bytes(message.getBody()));
      part.setCharset(CharacterSets.UTF_8);
      part.setContentType(ContentType.TEXT_PLAIN.getBytes());
      part.setContentId(name.getBytes());
      part.setContentLocation((name + ".txt").getBytes());
      part.setName((name + ".txt").getBytes());

      body.addPart(part);
      size += getPartSize(part);
    }

    for (Attachment attachment : scaledAttachments) {
      try {
        if (attachment.getUri() == null) throw new IOException("Assertion failed, attachment for outgoing MMS has no data!");

        String  fileName = attachment.fileName;
        PduPart part     = new PduPart();

        if (fileName == null) {
          fileName      = String.valueOf(Math.abs(new SecureRandom().nextLong()));
          String fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(attachment.contentType);

          if (fileExtension != null) fileName = fileName + "." + fileExtension;
        }

        if (attachment.contentType.startsWith("text")) {
          part.setCharset(CharacterSets.UTF_8);
        }

        part.setContentType(attachment.contentType.getBytes());
        part.setContentLocation(fileName.getBytes());
        part.setName(fileName.getBytes());

        int index = fileName.lastIndexOf(".");
        String contentId = (index == -1) ? fileName : fileName.substring(0, index);
        part.setContentId(contentId.getBytes());
        part.setData(StreamUtil.readFully(PartAuthority.getAttachmentStream(context, attachment.getUri())));

        body.addPart(part);
        size += getPartSize(part);
      } catch (IOException e) {
        Log.w(TAG, e);
      }
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    SmilXmlSerializer.serialize(SmilHelper.createSmilDocument(body), out);
    PduPart smilPart = new PduPart();
    smilPart.setContentId("smil".getBytes());
    smilPart.setContentLocation("smil.xml".getBytes());
    smilPart.setContentType(ContentType.APP_SMIL.getBytes());
    smilPart.setData(out.toByteArray());
    body.addPart(0, smilPart);

    req.setBody(body);
    req.setMessageSize(size);
    req.setMessageClass(PduHeaders.MESSAGE_CLASS_PERSONAL_STR.getBytes());
    req.setExpiry(7 * 24 * 60 * 60);

    try {
      req.setPriority(PduHeaders.PRIORITY_NORMAL);
      req.setDeliveryReport(PduHeaders.VALUE_NO);
      req.setReadReport(PduHeaders.VALUE_NO);
    } catch (InvalidHeaderValueException e) {}

    return req;
  }

  private long getPartSize(PduPart part) {
    return part.getName().length + part.getContentLocation().length +
        part.getContentType().length + part.getData().length +
        part.getContentId().length;
  }

  private void notifyMediaMessageDeliveryFailed(Context context, long messageId) {
    long      threadId  = SignalDatabase.messages().getThreadIdForMessage(messageId);
    Recipient recipient = SignalDatabase.threads().getRecipientForThreadId(threadId);

    if (recipient != null) {
      ApplicationDependencies.getMessageNotifier().notifyMessageDeliveryFailed(context, recipient, ConversationId.forConversation(threadId));
    }
  }

  private String getMyNumber(Context context) throws UndeliverableMessageException {
    try {
      return Utils.getMyPhoneNumber(context);
    } catch (SecurityException e) {
      throw new UndeliverableMessageException(e);
    }
  }

  public static class Factory implements Job.Factory<MmsSendJob> {
    @Override
    public @NonNull MmsSendJob create(@NonNull Parameters parameters, @Nullable byte[] serializedData) {
      JsonJobData data = JsonJobData.deserialize(serializedData);
      return new MmsSendJob(parameters, data.getLong(KEY_MESSAGE_ID));
    }
  }
}
