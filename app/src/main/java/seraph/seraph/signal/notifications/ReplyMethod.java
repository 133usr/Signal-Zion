package seraph.zion.signal.notifications;

import androidx.annotation.NonNull;

import seraph.zion.signal.recipients.Recipient;

public enum ReplyMethod {

  GroupMessage,
  SecureMessage;

  public static @NonNull ReplyMethod forRecipient(Recipient recipient) {
    if (recipient.isGroup()) {
      return ReplyMethod.GroupMessage;
    } else {
      return ReplyMethod.SecureMessage;
    }
  }
}
