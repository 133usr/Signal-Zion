package seraph.zion.signal.components.settings.models

import android.view.View
import seraph.zion.signal.R
import seraph.zion.signal.components.settings.PreferenceModel
import seraph.zion.signal.util.adapter.mapping.LayoutFactory
import seraph.zion.signal.util.adapter.mapping.MappingAdapter
import seraph.zion.signal.util.adapter.mapping.MappingViewHolder

object IndeterminateLoadingCircle : PreferenceModel<IndeterminateLoadingCircle>() {
  override fun areItemsTheSame(newItem: IndeterminateLoadingCircle): Boolean = true

  private class ViewHolder(itemView: View) : MappingViewHolder<IndeterminateLoadingCircle>(itemView) {
    override fun bind(model: IndeterminateLoadingCircle) = Unit
  }

  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(IndeterminateLoadingCircle::class.java, LayoutFactory({ ViewHolder(it) }, R.layout.indeterminate_loading_circle_pref))
  }
}
