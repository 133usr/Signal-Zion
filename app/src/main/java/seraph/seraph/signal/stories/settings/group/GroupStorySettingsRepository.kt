package seraph.zion.signal.stories.settings.group

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import seraph.zion.signal.database.GroupTable
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.groups.GroupId
import seraph.zion.signal.recipients.Recipient
import seraph.zion.signal.storage.StorageSyncHelper

class GroupStorySettingsRepository {
  fun unmarkAsGroupStory(groupId: GroupId): Completable {
    return Completable.fromAction {
      SignalDatabase.groups.setShowAsStoryState(groupId, GroupTable.ShowAsStoryState.NEVER)
      SignalDatabase.recipients.markNeedsSync(Recipient.externalGroupExact(groupId).id)
      StorageSyncHelper.scheduleSyncForDataChange()
    }.subscribeOn(Schedulers.io())
  }

  fun getConversationData(groupId: GroupId): Single<GroupConversationData> {
    return Single.fromCallable {
      val recipientId = SignalDatabase.recipients.getByGroupId(groupId).get()
      val threadId = SignalDatabase.threads.getThreadIdFor(recipientId) ?: -1L

      GroupConversationData(recipientId, threadId)
    }.subscribeOn(Schedulers.io())
  }
}
