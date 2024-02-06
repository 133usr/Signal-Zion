package seraph.zion.signal.components.settings.app.notifications.profiles.models

import android.view.View
import seraph.zion.signal.R
import seraph.zion.signal.components.settings.DSLSettingsIcon
import seraph.zion.signal.components.settings.DSLSettingsText
import seraph.zion.signal.components.settings.NO_TINT
import seraph.zion.signal.components.settings.PreferenceModel
import seraph.zion.signal.components.settings.PreferenceViewHolder
import seraph.zion.signal.recipients.RecipientId
import seraph.zion.signal.util.adapter.mapping.LayoutFactory
import seraph.zion.signal.util.adapter.mapping.MappingAdapter

/**
 * Custom DSL preference for adding members to a profile.
 */
object NotificationProfileAddMembers {

  fun register(adapter: MappingAdapter) {
    adapter.registerFactory(Model::class.java, LayoutFactory(::ViewHolder, R.layout.large_icon_preference_item))
  }

  class Model(
    override val title: DSLSettingsText = DSLSettingsText.from(R.string.AddAllowedMembers__add_people_or_groups),
    override val icon: DSLSettingsIcon = DSLSettingsIcon.from(R.drawable.add_to_a_group, NO_TINT),
    val onClick: (Long, Set<RecipientId>) -> Unit,
    val profileId: Long,
    val currentSelection: Set<RecipientId>
  ) : PreferenceModel<Model>() {
    override fun areContentsTheSame(newItem: Model): Boolean {
      return super.areContentsTheSame(newItem) && profileId == newItem.profileId && currentSelection == newItem.currentSelection
    }
  }

  private class ViewHolder(itemView: View) : PreferenceViewHolder<Model>(itemView) {
    override fun bind(model: Model) {
      super.bind(model)
      itemView.setOnClickListener { model.onClick(model.profileId, model.currentSelection) }
    }
  }
}
