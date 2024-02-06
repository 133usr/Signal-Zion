package seraph.zion.signal.avatar.vector

import seraph.zion.signal.avatar.Avatar
import seraph.zion.signal.avatar.AvatarColorItem
import seraph.zion.signal.avatar.Avatars

data class VectorAvatarCreationState(
  val currentAvatar: Avatar.Vector
) {
  fun colors(): List<AvatarColorItem> = Avatars.colors.map { AvatarColorItem(it, currentAvatar.color == it) }
}
