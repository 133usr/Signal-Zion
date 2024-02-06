package seraph.zion.signal.badges.gifts.viewgift

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.signal.libsignal.zkgroup.receipts.ReceiptCredentialPresentation
import seraph.zion.signal.badges.models.Badge
import seraph.zion.signal.components.settings.app.subscription.getBadge
import seraph.zion.signal.database.DatabaseObserver
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.database.model.MmsMessageRecord
import seraph.zion.signal.database.model.databaseprotos.GiftBadge
import seraph.zion.signal.dependencies.ApplicationDependencies
import java.util.Locale

/**
 * Shared repository for getting information about a particular gift.
 */
class ViewGiftRepository {
  fun getBadge(giftBadge: GiftBadge): Single<Badge> {
    val presentation = ReceiptCredentialPresentation(giftBadge.redemptionToken.toByteArray())
    return Single
      .fromCallable {
        ApplicationDependencies
          .getDonationsService()
          .getDonationsConfiguration(Locale.getDefault())
      }
      .flatMap { it.flattenResult() }
      .map { it.getBadge(presentation.receiptLevel.toInt()) }
      .subscribeOn(Schedulers.io())
  }

  fun getGiftBadge(messageId: Long): Observable<GiftBadge> {
    return Observable.create { emitter ->
      fun refresh() {
        val record = SignalDatabase.messages.getMessageRecord(messageId)
        val giftBadge: GiftBadge = (record as MmsMessageRecord).giftBadge!!

        emitter.onNext(giftBadge)
      }

      val messageObserver = DatabaseObserver.MessageObserver {
        if (messageId == it.id) {
          refresh()
        }
      }

      ApplicationDependencies.getDatabaseObserver().registerMessageUpdateObserver(messageObserver)
      emitter.setCancellable {
        ApplicationDependencies.getDatabaseObserver().unregisterObserver(messageObserver)
      }

      refresh()
    }
  }
}
