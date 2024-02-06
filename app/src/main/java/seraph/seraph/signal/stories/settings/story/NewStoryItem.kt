package seraph.zion.signal.stories.settings.story

import seraph.zion.signal.databinding.NewStoryItemBinding
import seraph.zion.signal.util.adapter.mapping.BindingFactory
import seraph.zion.signal.util.adapter.mapping.BindingViewHolder
import seraph.zion.signal.util.adapter.mapping.MappingAdapter
import seraph.zion.signal.util.adapter.mapping.MappingModel

/**
 * Entry point for new story creation.
 */
object NewStoryItem {
  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(Model::class.java, BindingFactory(::ViewHolder, NewStoryItemBinding::inflate))
  }

  class Model(val onClick: () -> Unit) : MappingModel<Model> {
    override fun areItemsTheSame(newItem: Model) = true
    override fun areContentsTheSame(newItem: Model) = true
  }

  private class ViewHolder(binding: NewStoryItemBinding) : BindingViewHolder<Model, NewStoryItemBinding>(binding) {
    override fun bind(model: Model) {
      binding.root.setOnClickListener {
        model.onClick()
      }
    }
  }
}
