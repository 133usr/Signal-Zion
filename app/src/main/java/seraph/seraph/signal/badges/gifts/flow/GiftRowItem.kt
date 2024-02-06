package seraph.zion.signal.badges.gifts.flow

import org.signal.core.util.money.FiatMoney
import seraph.zion.signal.R
import seraph.zion.signal.badges.models.Badge
import seraph.zion.signal.databinding.SubscriptionPreferenceBinding
import seraph.zion.signal.payments.FiatMoneyUtil
import seraph.zion.signal.util.adapter.mapping.BindingFactory
import seraph.zion.signal.util.adapter.mapping.BindingViewHolder
import seraph.zion.signal.util.adapter.mapping.MappingAdapter
import seraph.zion.signal.util.adapter.mapping.MappingModel
import seraph.zion.signal.util.visible
import java.util.concurrent.TimeUnit

/**
 * A line item for gifts, displayed in the Gift flow's start and confirmation fragments.
 */
object GiftRowItem {
  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(Model::class.java, BindingFactory(::ViewHolder, SubscriptionPreferenceBinding::inflate))
  }

  class Model(val giftBadge: Badge, val price: FiatMoney) : MappingModel<Model> {
    override fun areItemsTheSame(newItem: Model): Boolean = giftBadge.id == newItem.giftBadge.id

    override fun areContentsTheSame(newItem: Model): Boolean = giftBadge == newItem.giftBadge && price == newItem.price
  }

  class ViewHolder(binding: SubscriptionPreferenceBinding) : BindingViewHolder<Model, SubscriptionPreferenceBinding>(binding) {
    init {
      binding.root.isSelected = true
    }

    override fun bind(model: Model) {
      binding.check.visible = false
      binding.badge.setBadge(model.giftBadge)
      binding.tagline.visible = true

      val price = FiatMoneyUtil.format(
        context.resources,
        model.price,
        FiatMoneyUtil.formatOptions()
          .trimZerosAfterDecimal()
          .withDisplayTime(false)
      )

      val duration = TimeUnit.MILLISECONDS.toDays(model.giftBadge.duration)

      binding.title.text = model.giftBadge.name
      binding.tagline.text = context.resources.getQuantityString(R.plurals.GiftRowItem_s_dot_d_day_duration, duration.toInt(), price, duration)
    }
  }
}
