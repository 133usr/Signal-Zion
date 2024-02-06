package seraph.zion.signal.reactions

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.schedulers.Schedulers
import seraph.zion.signal.components.emoji.EmojiUtil
import seraph.zion.signal.database.DatabaseObserver
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.database.model.MessageId
import seraph.zion.signal.database.model.ReactionRecord
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.recipients.Recipient

class ReactionsRepository {

  fun getReactions(messageId: MessageId): Observable<List<ReactionDetails>> {
    return Observable.create { emitter: ObservableEmitter<List<ReactionDetails>> ->
      val databaseObserver: DatabaseObserver = ApplicationDependencies.getDatabaseObserver()

      val messageObserver = DatabaseObserver.MessageObserver { reactionMessageId ->
        if (reactionMessageId == messageId) {
          emitter.onNext(fetchReactionDetails(reactionMessageId))
        }
      }

      databaseObserver.registerMessageUpdateObserver(messageObserver)

      emitter.setCancellable {
        databaseObserver.unregisterObserver(messageObserver)
      }

      emitter.onNext(fetchReactionDetails(messageId))
    }.subscribeOn(Schedulers.io())
  }

  private fun fetchReactionDetails(messageId: MessageId): List<ReactionDetails> {
    val reactions: List<ReactionRecord> = SignalDatabase.reactions.getReactions(messageId)

    return reactions.map { reaction ->
      ReactionDetails(
        sender = Recipient.resolved(reaction.author),
        baseEmoji = EmojiUtil.getCanonicalRepresentation(reaction.emoji),
        displayEmoji = reaction.emoji,
        timestamp = reaction.dateReceived
      )
    }
  }
}
