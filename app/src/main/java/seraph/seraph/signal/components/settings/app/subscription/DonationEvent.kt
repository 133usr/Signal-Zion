package seraph.zion.signal.components.settings.app.subscription

import seraph.zion.signal.badges.models.Badge

/**
 * Events that can arise from use of the donations apis.
 */
sealed class DonationEvent {
  object RequestTokenSuccess : DonationEvent()
  class PaymentConfirmationSuccess(val badge: Badge) : DonationEvent()
  class SubscriptionCancellationFailed(val throwable: Throwable) : DonationEvent()
  object SubscriptionCancelled : DonationEvent()
}
