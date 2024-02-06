package seraph.zion.signal.badges.models

import android.view.View
import android.widget.TextView
import seraph.zion.signal.R
import seraph.zion.signal.badges.BadgeImageView
import seraph.zion.signal.util.adapter.mapping.LayoutFactory
import seraph.zion.signal.util.adapter.mapping.MappingAdapter
import seraph.zion.signal.util.adapter.mapping.MappingModel
import seraph.zion.signal.util.adapter.mapping.MappingViewHolder

/**
 * Displays a 160dp badge.
 */
object BadgeDisplay160 {
  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(Model::class.java, LayoutFactory(::ViewHolder, R.layout.badge_display_160))
  }

  class Model(val badge: Badge) : MappingModel<Model> {
    override fun areItemsTheSame(newItem: Model): Boolean = badge.id == newItem.badge.id

    override fun areContentsTheSame(newItem: Model): Boolean = badge == newItem.badge
  }

  class ViewHolder(itemView: View) : MappingViewHolder<Model>(itemView) {
    private val badgeImageView: BadgeImageView = itemView.findViewById(R.id.badge)
    private val titleView: TextView = itemView.findViewById(R.id.name)

    override fun bind(model: Model) {
      titleView.text = model.badge.name
      badgeImageView.setBadge(model.badge)
    }
  }
}
