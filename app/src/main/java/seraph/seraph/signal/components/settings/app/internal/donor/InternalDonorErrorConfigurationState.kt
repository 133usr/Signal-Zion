package seraph.zion.signal.components.settings.app.internal.donor

import org.signal.donations.StripeDeclineCode
import seraph.zion.signal.badges.models.Badge
import seraph.zion.signal.components.settings.app.subscription.errors.UnexpectedSubscriptionCancellation

data class InternalDonorErrorConfigurationState(
  val badges: List<Badge> = emptyList(),
  val selectedBadge: Badge? = null,
  val selectedUnexpectedSubscriptionCancellation: UnexpectedSubscriptionCancellation? = null,
  val selectedStripeDeclineCode: StripeDeclineCode.Code? = null
)
