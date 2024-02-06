package seraph.zion.signal.stories.viewer.views

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.signal.core.util.logging.Log
import seraph.zion.signal.database.DatabaseObserver
import seraph.zion.signal.database.GroupReceiptTable
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.database.model.MessageRecord
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.keyvalue.SignalStore
import seraph.zion.signal.recipients.Recipient
import seraph.zion.signal.recipients.RecipientId
import org.whispersystems.signalservice.api.push.DistributionId

class StoryViewsRepository {

  companion object {
    private val TAG = Log.tag(StoryViewsRepository::class.java)
  }

  fun isReadReceiptsEnabled(): Boolean = SignalStore.storyValues().viewedReceiptsEnabled

  fun getStoryRecipient(storyId: Long): Single<Recipient> {
    return Single.fromCallable {
      SignalDatabase.messages.getMessageRecord(storyId).toRecipient
    }.subscribeOn(Schedulers.io())
  }

  fun getViews(storyId: Long): Observable<List<StoryViewItemData>> {
    return Observable.create<List<StoryViewItemData>> { emitter ->
      val record: MessageRecord = SignalDatabase.messages.getMessageRecord(storyId)
      val filterIds: Set<RecipientId> = if (record.toRecipient.isDistributionList) {
        val distributionId: DistributionId = SignalDatabase.distributionLists.getDistributionId(record.toRecipient.requireDistributionListId())!!
        SignalDatabase.storySends.getRecipientsForDistributionId(storyId, distributionId)
      } else {
        emptySet()
      }

      fun refresh() {
        emitter.onNext(
          SignalDatabase.groupReceipts.getGroupReceiptInfo(storyId).filter {
            it.status == GroupReceiptTable.STATUS_VIEWED
          }.filter {
            filterIds.isEmpty() || it.recipientId in filterIds
          }.map {
            StoryViewItemData(
              recipient = Recipient.resolved(it.recipientId),
              timeViewedInMillis = it.timestamp
            )
          }
        )
      }

      val observer = DatabaseObserver.MessageObserver { refresh() }

      ApplicationDependencies.getDatabaseObserver().registerMessageUpdateObserver(observer)
      emitter.setCancellable {
        ApplicationDependencies.getDatabaseObserver().unregisterObserver(observer)
      }

      refresh()
    }.subscribeOn(Schedulers.io())
  }

  fun removeUserFromStory(user: Recipient, story: Recipient): Completable {
    return Completable.fromAction {
      val distributionListRecord = SignalDatabase.distributionLists.getList(story.requireDistributionListId())!!
      if (user.id in distributionListRecord.members) {
        SignalDatabase.distributionLists.excludeFromStory(user.id, distributionListRecord)
      } else {
        Log.w(TAG, "User is no longer in the distribution list.")
      }
    }
  }
}
