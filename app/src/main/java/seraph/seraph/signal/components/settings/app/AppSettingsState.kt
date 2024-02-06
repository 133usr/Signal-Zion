package seraph.zion.signal.components.settings.app

import seraph.zion.signal.recipients.Recipient

data class AppSettingsState(
  val self: Recipient,
  val unreadPaymentsCount: Int,
  val hasExpiredGiftBadge: Boolean,
  val allowUserToGoToDonationManagementScreen: Boolean,
  val userUnregistered: Boolean,
  val clientDeprecated: Boolean
) {
  fun isDeprecatedOrUnregistered(): Boolean {
    return !(userUnregistered || clientDeprecated)
  }
}
