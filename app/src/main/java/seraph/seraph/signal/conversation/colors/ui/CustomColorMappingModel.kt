package seraph.zion.signal.conversation.colors.ui

import seraph.zion.signal.util.adapter.mapping.MappingModel

class CustomColorMappingModel : MappingModel<CustomColorMappingModel> {
  override fun areItemsTheSame(newItem: CustomColorMappingModel): Boolean {
    return true
  }

  override fun areContentsTheSame(newItem: CustomColorMappingModel): Boolean {
    return true
  }
}
