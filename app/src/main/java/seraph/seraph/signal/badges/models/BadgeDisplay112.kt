package seraph.zion.signal.badges.models

import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import seraph.zion.signal.R
import seraph.zion.signal.badges.BadgeImageView
import seraph.zion.signal.database.model.databaseprotos.GiftBadge
import seraph.zion.signal.util.adapter.mapping.LayoutFactory
import seraph.zion.signal.util.adapter.mapping.MappingAdapter
import seraph.zion.signal.util.adapter.mapping.MappingModel
import seraph.zion.signal.util.adapter.mapping.MappingViewHolder
import seraph.zion.signal.util.visible

/**
 * Displays a 112dp badge.
 */
object BadgeDisplay112 {
  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(Model::class.java, LayoutFactory(::ViewHolder, R.layout.badge_display_112))
    mappingAdapter.registerFactory(GiftModel::class.java, LayoutFactory(::GiftViewHolder, R.layout.badge_display_112))
  }

  class Model(val badge: Badge, val withDisplayText: Boolean = true) : MappingModel<Model> {
    override fun areItemsTheSame(newItem: Model): Boolean = badge.id == newItem.badge.id

    override fun areContentsTheSame(newItem: Model): Boolean = badge == newItem.badge && withDisplayText == newItem.withDisplayText
  }

  class GiftModel(val giftBadge: GiftBadge) : MappingModel<GiftModel> {
    override fun areItemsTheSame(newItem: GiftModel): Boolean = giftBadge.redemptionToken == newItem.giftBadge.redemptionToken
    override fun areContentsTheSame(newItem: GiftModel): Boolean = giftBadge == newItem.giftBadge
  }

  class ViewHolder(itemView: View) : MappingViewHolder<Model>(itemView) {
    private val badgeImageView: BadgeImageView = itemView.findViewById(R.id.badge)
    private val titleView: TextView = itemView.findViewById(R.id.name)

    override fun bind(model: Model) {
      titleView.text = model.badge.name
      titleView.visible = model.withDisplayText
      badgeImageView.setBadge(model.badge)
    }
  }

  class GiftViewHolder(itemView: View) : MappingViewHolder<GiftModel>(itemView) {
    private val badgeImageView: BadgeImageView = itemView.findViewById(R.id.badge)
    private val titleView: TextView = itemView.findViewById(R.id.name)

    override fun bind(model: GiftModel) {
      titleView.visible = false
      badgeImageView.setGiftBadge(model.giftBadge, Glide.with(badgeImageView))
    }
  }
}
