package seraph.zion.signal.profiles.manage

import seraph.zion.signal.databinding.CopyButtonBinding
import seraph.zion.signal.util.adapter.mapping.BindingFactory
import seraph.zion.signal.util.adapter.mapping.BindingViewHolder
import seraph.zion.signal.util.adapter.mapping.MappingAdapter
import seraph.zion.signal.util.adapter.mapping.MappingModel

/**
 * Outlined button that allows the user to copy a piece of data.
 */
object CopyButton {
  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(Model::class.java, BindingFactory(::ViewHolder, CopyButtonBinding::inflate))
  }

  class Model(
    val text: CharSequence,
    val onClick: (Model) -> Unit
  ) : MappingModel<Model> {
    override fun areItemsTheSame(newItem: Model): Boolean = true

    override fun areContentsTheSame(newItem: Model): Boolean = text == newItem.text
  }

  private class ViewHolder(binding: CopyButtonBinding) : BindingViewHolder<Model, CopyButtonBinding>(binding) {
    override fun bind(model: Model) {
      binding.root.text = model.text
      binding.root.setOnClickListener { model.onClick(model) }
    }
  }
}
