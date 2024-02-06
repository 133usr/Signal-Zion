package seraph.zion.signal.badges.gifts.flow

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.signal.core.util.logging.Log
import org.signal.core.util.money.FiatMoney
import seraph.zion.signal.badges.models.Badge
import seraph.zion.signal.components.settings.app.subscription.getGiftBadgeAmounts
import seraph.zion.signal.components.settings.app.subscription.getGiftBadges
import seraph.zion.signal.dependencies.ApplicationDependencies
import org.whispersystems.signalservice.internal.push.DonationsConfiguration
import java.util.Currency
import java.util.Locale

/**
 * Repository for grabbing gift badges and supported currency information.
 */
class GiftFlowRepository {

  companion object {
    private val TAG = Log.tag(GiftFlowRepository::class.java)
  }

  fun getGiftBadge(): Single<Pair<Int, Badge>> {
    return Single
      .fromCallable {
        ApplicationDependencies.getDonationsService()
          .getDonationsConfiguration(Locale.getDefault())
      }
      .flatMap { it.flattenResult() }
      .map { DonationsConfiguration.GIFT_LEVEL to it.getGiftBadges().first() }
      .subscribeOn(Schedulers.io())
  }

  fun getGiftPricing(): Single<Map<Currency, FiatMoney>> {
    return Single
      .fromCallable {
        ApplicationDependencies.getDonationsService()
          .getDonationsConfiguration(Locale.getDefault())
      }
      .subscribeOn(Schedulers.io())
      .flatMap { it.flattenResult() }
      .map { it.getGiftBadgeAmounts() }
  }
}
