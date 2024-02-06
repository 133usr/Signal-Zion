package seraph.zion.signal.components.settings.conversation.preferences

import android.view.View
import androidx.core.view.ViewCompat
import seraph.zion.signal.R
import seraph.zion.signal.avatar.view.AvatarView
import seraph.zion.signal.badges.BadgeImageView
import seraph.zion.signal.badges.models.Badge
import seraph.zion.signal.components.settings.PreferenceModel
import seraph.zion.signal.contacts.avatars.FallbackContactPhoto
import seraph.zion.signal.contacts.avatars.FallbackPhoto
import seraph.zion.signal.database.model.StoryViewState
import seraph.zion.signal.recipients.Recipient
import seraph.zion.signal.util.ViewUtil
import seraph.zion.signal.util.adapter.mapping.LayoutFactory
import seraph.zion.signal.util.adapter.mapping.MappingAdapter
import seraph.zion.signal.util.adapter.mapping.MappingViewHolder

/**
 * Renders a large avatar (80dp) for a given Recipient.
 */
object AvatarPreference {

  fun register(adapter: MappingAdapter) {
    adapter.registerFactory(Model::class.java, LayoutFactory(::ViewHolder, R.layout.conversation_settings_avatar_preference_item))
  }

  class Model(
    val recipient: Recipient,
    val storyViewState: StoryViewState,
    val onAvatarClick: (AvatarView) -> Unit,
    val onBadgeClick: (Badge) -> Unit
  ) : PreferenceModel<Model>() {
    override fun areItemsTheSame(newItem: Model): Boolean {
      return recipient == newItem.recipient
    }

    override fun areContentsTheSame(newItem: Model): Boolean {
      return super.areContentsTheSame(newItem) &&
        recipient.hasSameContent(newItem.recipient) &&
        storyViewState == newItem.storyViewState
    }
  }

  private class ViewHolder(itemView: View) : MappingViewHolder<Model>(itemView) {
    private val avatar: AvatarView = itemView.findViewById<AvatarView>(R.id.bio_preference_avatar).apply {
      setFallbackPhotoProvider(AvatarPreferenceFallbackPhotoProvider())
    }

    private val badge: BadgeImageView = itemView.findViewById(R.id.bio_preference_badge)

    init {
      ViewCompat.setTransitionName(avatar.parent as View, "avatar")
    }

    override fun bind(model: Model) {
      if (model.recipient.isSelf) {
        badge.setBadge(null)
        badge.setOnClickListener(null)
      } else {
        badge.setBadgeFromRecipient(model.recipient)
        badge.setOnClickListener {
          val badge = model.recipient.badges.firstOrNull()
          if (badge != null) {
            model.onBadgeClick(badge)
          }
        }
      }

      avatar.setStoryRingFromState(model.storyViewState)
      avatar.displayChatAvatar(model.recipient)
      avatar.disableQuickContact()
      avatar.setOnClickListener { model.onAvatarClick(avatar) }
    }
  }

  private class AvatarPreferenceFallbackPhotoProvider : Recipient.FallbackPhotoProvider() {
    override fun getPhotoForGroup(): FallbackContactPhoto {
      return FallbackPhoto(R.drawable.ic_group_outline_40, ViewUtil.dpToPx(8))
    }
  }
}
