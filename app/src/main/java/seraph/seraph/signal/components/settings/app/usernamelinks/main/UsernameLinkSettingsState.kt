package seraph.zion.signal.components.settings.app.usernamelinks.main

import seraph.zion.signal.components.settings.app.usernamelinks.QrCodeState
import seraph.zion.signal.components.settings.app.usernamelinks.UsernameQrCodeColorScheme

/**
 * Represents the UI state of the [UsernameLinkSettingsFragment].
 */
data class UsernameLinkSettingsState(
  val activeTab: ActiveTab,
  val username: String,
  val usernameLinkState: UsernameLinkState,
  val qrCodeState: QrCodeState,
  val qrCodeColorScheme: UsernameQrCodeColorScheme,
  val qrScanResult: QrScanResult? = null,
  val usernameLinkResetResult: UsernameLinkResetResult? = null,
  val indeterminateProgress: Boolean = false
) {
  enum class ActiveTab {
    Code, Scan
  }
}
