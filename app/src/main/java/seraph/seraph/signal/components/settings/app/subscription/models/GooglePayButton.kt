package seraph.zion.signal.components.settings.app.subscription.models

import android.view.View
import seraph.zion.signal.R
import seraph.zion.signal.components.settings.PreferenceModel
import seraph.zion.signal.util.adapter.mapping.LayoutFactory
import seraph.zion.signal.util.adapter.mapping.MappingAdapter
import seraph.zion.signal.util.adapter.mapping.MappingViewHolder

object GooglePayButton {

  class Model(val onClick: () -> Unit, override val isEnabled: Boolean) : PreferenceModel<Model>(isEnabled = isEnabled) {
    override fun areItemsTheSame(newItem: Model): Boolean = true
  }

  class ViewHolder(itemView: View) : MappingViewHolder<Model>(itemView) {

    private val googlePayButton: View = findViewById(R.id.googlepay_button)

    override fun bind(model: Model) {
      googlePayButton.isEnabled = model.isEnabled
      googlePayButton.setOnClickListener {
        googlePayButton.isEnabled = false
        model.onClick()
      }
    }
  }

  fun register(adapter: MappingAdapter) {
    adapter.registerFactory(Model::class.java, LayoutFactory({ ViewHolder(it) }, R.layout.google_pay_button_pref))
  }
}
