package seraph.zion.signal.components.settings.app.internal

import android.content.Context
import org.json.JSONObject
import org.signal.core.util.concurrent.SignalExecutors
import seraph.zion.signal.database.MessageTable
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.database.model.RemoteMegaphoneRecord
import seraph.zion.signal.database.model.addStyle
import seraph.zion.signal.database.model.databaseprotos.BodyRangeList
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.emoji.EmojiFiles
import seraph.zion.signal.jobs.AttachmentDownloadJob
import seraph.zion.signal.jobs.CreateReleaseChannelJob
import seraph.zion.signal.jobs.FetchRemoteMegaphoneImageJob
import seraph.zion.signal.keyvalue.SignalStore
import seraph.zion.signal.notifications.v2.ConversationId
import seraph.zion.signal.recipients.Recipient
import seraph.zion.signal.releasechannel.ReleaseChannel
import java.util.UUID
import kotlin.time.Duration.Companion.days

class InternalSettingsRepository(context: Context) {

  private val context = context.applicationContext

  fun getEmojiVersionInfo(consumer: (EmojiFiles.Version?) -> Unit) {
    SignalExecutors.BOUNDED.execute {
      consumer(EmojiFiles.Version.readVersion(context))
    }
  }

  fun addSampleReleaseNote() {
    SignalExecutors.UNBOUNDED.execute {
      ApplicationDependencies.getJobManager().runSynchronously(CreateReleaseChannelJob.create(), 5000)

      val title = "Release Note Title"
      val bodyText = "Release note body. Aren't I awesome?"
      val body = "$title\n\n$bodyText"
      val bodyRangeList = BodyRangeList.Builder()
        .addStyle(BodyRangeList.BodyRange.Style.BOLD, 0, title.length)

      val recipientId = SignalStore.releaseChannelValues().releaseChannelRecipientId!!
      val threadId = SignalDatabase.threads.getOrCreateThreadIdFor(Recipient.resolved(recipientId))

      val insertResult: MessageTable.InsertResult? = ReleaseChannel.insertReleaseChannelMessage(
        recipientId = recipientId,
        body = body,
        threadId = threadId,
        messageRanges = bodyRangeList.build(),
        media = "/static/release-notes/signal.png",
        mediaWidth = 1800,
        mediaHeight = 720
      )

      SignalDatabase.messages.insertBoostRequestMessage(recipientId, threadId)

      if (insertResult != null) {
        SignalDatabase.attachments.getAttachmentsForMessage(insertResult.messageId)
          .forEach { ApplicationDependencies.getJobManager().add(AttachmentDownloadJob(insertResult.messageId, it.attachmentId, false)) }

        ApplicationDependencies.getMessageNotifier().updateNotification(context, ConversationId.forConversation(insertResult.threadId))
      }
    }
  }

  fun addRemoteMegaphone(actionId: RemoteMegaphoneRecord.ActionId) {
    SignalExecutors.UNBOUNDED.execute {
      val record = RemoteMegaphoneRecord(
        uuid = UUID.randomUUID().toString(),
        priority = 100,
        countries = "*:1000000",
        minimumVersion = 1,
        doNotShowBefore = System.currentTimeMillis() - 2.days.inWholeMilliseconds,
        doNotShowAfter = System.currentTimeMillis() + 28.days.inWholeMilliseconds,
        showForNumberOfDays = 30,
        conditionalId = null,
        primaryActionId = actionId,
        secondaryActionId = RemoteMegaphoneRecord.ActionId.SNOOZE,
        imageUrl = "/static/release-notes/donate-heart.png",
        title = "Donate Test",
        body = "Donate body test.",
        primaryActionText = "Donate",
        secondaryActionText = "Snooze",
        primaryActionData = null,
        secondaryActionData = JSONObject("{ \"snoozeDurationDays\": [5, 7, 100] }")
      )

      SignalDatabase.remoteMegaphones.insert(record)

      if (record.imageUrl != null) {
        ApplicationDependencies.getJobManager().add(FetchRemoteMegaphoneImageJob(record.uuid, record.imageUrl))
      }
    }
  }
}
