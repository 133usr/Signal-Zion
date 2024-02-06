package seraph.zion.signal.components.settings.conversation.permissions

import seraph.zion.signal.groups.ui.GroupChangeFailureReason

sealed class PermissionsSettingsEvents {
  class GroupChangeError(val reason: GroupChangeFailureReason) : PermissionsSettingsEvents()
}
