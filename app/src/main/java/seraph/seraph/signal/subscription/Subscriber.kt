package seraph.zion.signal.subscription

import org.whispersystems.signalservice.api.subscriptions.SubscriberId

data class Subscriber(
  val subscriberId: SubscriberId,
  val currencyCode: String
)
