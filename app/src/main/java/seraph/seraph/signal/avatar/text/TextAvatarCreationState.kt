package seraph.zion.signal.avatar.text

import seraph.zion.signal.avatar.Avatar
import seraph.zion.signal.avatar.AvatarColorItem
import seraph.zion.signal.avatar.Avatars

data class TextAvatarCreationState(
  val currentAvatar: Avatar.Text
) {
  fun colors(): List<AvatarColorItem> = Avatars.colors.map { AvatarColorItem(it, currentAvatar.color == it) }
}
