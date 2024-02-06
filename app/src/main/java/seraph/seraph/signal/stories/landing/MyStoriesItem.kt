package seraph.zion.signal.stories.landing

import android.view.View
import seraph.zion.signal.R
import seraph.zion.signal.avatar.view.AvatarView
import seraph.zion.signal.components.settings.PreferenceModel
import seraph.zion.signal.recipients.Recipient
import seraph.zion.signal.util.adapter.mapping.LayoutFactory
import seraph.zion.signal.util.adapter.mapping.MappingAdapter
import seraph.zion.signal.util.adapter.mapping.MappingViewHolder

/**
 * Item displayed on an empty Stories landing page allowing the user to add a new story.
 */
object MyStoriesItem {

  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(Model::class.java, LayoutFactory(::ViewHolder, R.layout.stories_landing_item_my_stories))
  }

  class Model(
    val onClick: () -> Unit
  ) : PreferenceModel<Model>() {
    override fun areItemsTheSame(newItem: Model): Boolean = true
  }

  private class ViewHolder(itemView: View) : MappingViewHolder<Model>(itemView) {

    private val avatarView: AvatarView = itemView.findViewById(R.id.avatar)

    override fun bind(model: Model) {
      itemView.setOnClickListener { model.onClick() }
      avatarView.displayProfileAvatar(Recipient.self())
    }
  }
}
