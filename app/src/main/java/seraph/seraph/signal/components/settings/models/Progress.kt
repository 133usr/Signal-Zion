package seraph.zion.signal.components.settings.models

import android.view.View
import android.widget.TextView
import seraph.zion.signal.R
import seraph.zion.signal.components.settings.DSLSettingsText
import seraph.zion.signal.components.settings.PreferenceModel
import seraph.zion.signal.util.adapter.mapping.LayoutFactory
import seraph.zion.signal.util.adapter.mapping.MappingAdapter
import seraph.zion.signal.util.adapter.mapping.MappingViewHolder
import seraph.zion.signal.util.visible

object Progress {

  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(Model::class.java, LayoutFactory(::ViewHolder, R.layout.dsl_progress_pref))
  }

  data class Model(
    override val title: DSLSettingsText?
  ) : PreferenceModel<Model>()

  private class ViewHolder(itemView: View) : MappingViewHolder<Model>(itemView) {

    private val title: TextView = itemView.findViewById(R.id.dsl_progress_pref_title)

    override fun bind(model: Model) {
      title.text = model.title?.resolve(context)
      title.visible = model.title != null
    }
  }
}
