package seraph.zion.signal.conversation.ui.mentions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import seraph.zion.signal.database.GroupTable;
import seraph.zion.signal.database.RecipientTable;
import seraph.zion.signal.database.SignalDatabase;
import seraph.zion.signal.recipients.Recipient;
import seraph.zion.signal.recipients.RecipientId;

import java.util.Collections;
import java.util.List;

final class MentionsPickerRepository {

  private final RecipientTable recipientTable;
  private final GroupTable     groupDatabase;

  MentionsPickerRepository() {
    recipientTable = SignalDatabase.recipients();
    groupDatabase  = SignalDatabase.groups();
  }

  @WorkerThread
  @NonNull List<RecipientId> getMembers(@Nullable Recipient recipient) {
    if (recipient == null || !recipient.isPushV2Group()) {
      return Collections.emptyList();
    }

    return groupDatabase.getGroupMemberIds(recipient.requireGroupId(), GroupTable.MemberSet.FULL_MEMBERS_EXCLUDING_SELF);
  }

  @WorkerThread
  @NonNull List<Recipient> search(@NonNull MentionQuery mentionQuery) {
    if (mentionQuery.query == null || mentionQuery.members.isEmpty()) {
      return Collections.emptyList();
    }

    return recipientTable.queryRecipientsForMentions(mentionQuery.query, mentionQuery.members);
  }

  static class MentionQuery {
    @Nullable private final String            query;
    @NonNull  private final List<RecipientId> members;

    MentionQuery(@Nullable String query, @NonNull List<RecipientId> members) {
      this.query   = query;
      this.members = members;
    }
  }
}
