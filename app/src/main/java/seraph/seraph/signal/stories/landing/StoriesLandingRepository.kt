package seraph.zion.signal.stories.landing

import android.content.Context
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.signal.core.util.concurrent.SignalExecutors
import seraph.zion.signal.conversation.ConversationMessage
import seraph.zion.signal.database.DatabaseObserver
import seraph.zion.signal.database.MessageTable
import seraph.zion.signal.database.RxDatabaseObserver
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.database.model.DistributionListId
import seraph.zion.signal.database.model.MessageRecord
import seraph.zion.signal.database.model.StoryResult
import seraph.zion.signal.database.model.StoryViewState
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.jobs.MultiDeviceReadUpdateJob
import seraph.zion.signal.keyvalue.SignalStore
import seraph.zion.signal.recipients.Recipient
import seraph.zion.signal.recipients.RecipientForeverObserver
import seraph.zion.signal.recipients.RecipientId
import seraph.zion.signal.sms.MessageSender

class StoriesLandingRepository(context: Context) {

  private val context = context.applicationContext

  fun resend(story: MessageRecord): Completable {
    return Completable.fromAction {
      MessageSender.resend(context, story)
    }.subscribeOn(Schedulers.io())
  }

  @Suppress("UsePropertyAccessSyntax")
  fun getStories(): Observable<List<StoriesLandingItemData>> {
    val storyRecipients: Observable<Map<Recipient, List<StoryResult>>> = RxDatabaseObserver
      .conversationList
      .toObservable()
      .map {
        val myStoriesId = SignalDatabase.recipients.getOrInsertFromDistributionListId(DistributionListId.MY_STORY)
        val myStories = Recipient.resolved(myStoriesId)

        val stories = SignalDatabase.messages.getOrderedStoryRecipientsAndIds(false)
        val mapping: MutableMap<Recipient, List<StoryResult>> = mutableMapOf()

        stories.forEach {
          val recipient = Recipient.resolved(it.recipientId)
          if (recipient.isDistributionList || (it.isOutgoing && !recipient.isInactiveGroup())) {
            val list = mapping[myStories] ?: emptyList()
            mapping[myStories] = list + it
          }

          if (!recipient.isDistributionList && !recipient.isBlocked && !recipient.isInactiveGroup()) {
            val list = mapping[recipient] ?: emptyList()
            mapping[recipient] = list + it
          }
        }

        mapping
      }

    return storyRecipients.switchMap { map ->
      val observables = map.map { (recipient, results) ->
        val messages = results
          .sortedBy { it.messageSentTimestamp }
          .reversed()
          .take(if (recipient.isMyStory) 2 else 1)
          .map {
            SignalDatabase.messages.getMessageRecord(it.messageId)
          }

        var sendingCount: Long = 0
        var failureCount: Long = 0

        if (recipient.isMyStory) {
          SignalDatabase.messages.getMessages(results.map { it.messageId }).use { reader ->
            var messageRecord: MessageRecord? = reader.getNext()
            while (messageRecord != null) {
              if (messageRecord.isOutgoing && (messageRecord.isPending || messageRecord.isMediaPending)) {
                sendingCount++
              } else if (messageRecord.isFailed) {
                failureCount++
              }

              messageRecord = reader.getNext()
            }
          }
        }

        createStoriesLandingItemData(recipient, messages, sendingCount, failureCount)
      }

      if (observables.isEmpty()) {
        Observable.just(emptyList())
      } else {
        Observable.combineLatest(observables) {
          it.filterIsInstance<StoriesLandingItemData>()
        }
      }
    }.subscribeOn(Schedulers.io())
  }

  private fun createStoriesLandingItemData(sender: Recipient, messageRecords: List<MessageRecord>, sendingCount: Long, failureCount: Long): Observable<StoriesLandingItemData> {
    val itemDataObservable = Observable.create<StoriesLandingItemData> { emitter ->
      fun refresh(sender: Recipient) {
        val primaryIndex = messageRecords.indexOfFirst { !it.isOutgoing && !it.isViewed }.takeIf { it > -1 } ?: 0
        val itemData = StoriesLandingItemData(
          storyRecipient = sender,
          storyViewState = StoryViewState.NONE,
          hasReplies = messageRecords.any { SignalDatabase.messages.getNumberOfStoryReplies(it.id) > 0 },
          hasRepliesFromSelf = messageRecords.any { SignalDatabase.messages.hasSelfReplyInStory(it.id) },
          isHidden = sender.shouldHideStory(),
          primaryStory = ConversationMessage.ConversationMessageFactory.createWithUnresolvedData(context, messageRecords[primaryIndex], sender),
          secondaryStory = if (sender.isMyStory) {
            messageRecords.drop(1).firstOrNull()?.let {
              ConversationMessage.ConversationMessageFactory.createWithUnresolvedData(context, it, sender)
            }
          } else {
            null
          },
          sendingCount = sendingCount,
          failureCount = failureCount
        )

        emitter.onNext(itemData)
      }

      val newRepliesObserver = DatabaseObserver.Observer {
        Recipient.live(sender.id).refresh()
      }

      val recipientChangedObserver = RecipientForeverObserver {
        refresh(it)
      }

      ApplicationDependencies.getDatabaseObserver().registerConversationObserver(messageRecords.first().threadId, newRepliesObserver)
      val liveRecipient = Recipient.live(sender.id)
      liveRecipient.observeForever(recipientChangedObserver)

      emitter.setCancellable {
        ApplicationDependencies.getDatabaseObserver().unregisterObserver(newRepliesObserver)
        liveRecipient.removeForeverObserver(recipientChangedObserver)
      }

      refresh(sender)
    }

    val storyViewedStateObservable = StoryViewState.getForRecipientId(if (sender.isMyStory) Recipient.self().id else sender.id)

    return Observable.combineLatest(itemDataObservable, storyViewedStateObservable) { data, state ->
      data.copy(storyViewState = state)
    }
  }

  fun setHideStory(recipientId: RecipientId, hideStory: Boolean): Completable {
    return Completable.fromAction {
      SignalDatabase.recipients.setHideStory(recipientId, hideStory)
    }.subscribeOn(Schedulers.io())
  }

  /**
   * Marks all stories as "seen" by the user (marking them as read in the database)
   */
  fun markStoriesRead() {
    SignalExecutors.BOUNDED_IO.execute {
      val messageInfos: List<MessageTable.MarkedMessageInfo> = SignalDatabase.messages.markAllIncomingStoriesRead()
      val releaseThread: Long? = SignalStore.releaseChannelValues().releaseChannelRecipientId?.let { SignalDatabase.threads.getThreadIdIfExistsFor(it) }

      MultiDeviceReadUpdateJob.enqueue(messageInfos.filter { it.threadId == releaseThread }.map { it.syncMessageId })
    }
  }

  /**
   * Marks all failed stories as "notified" by the user (marking them as notified in the database)
   */
  fun markFailedStoriesNotified() {
    SignalExecutors.BOUNDED_IO.execute {
      SignalDatabase.messages.markAllFailedStoriesNotified()
    }
  }
}
