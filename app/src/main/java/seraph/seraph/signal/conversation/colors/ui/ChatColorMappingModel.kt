package seraph.zion.signal.conversation.colors.ui

import seraph.zion.signal.conversation.colors.ChatColors
import seraph.zion.signal.util.adapter.mapping.MappingModel

class ChatColorMappingModel(
  val chatColors: ChatColors,
  val isSelected: Boolean,
  val isAuto: Boolean
) : MappingModel<ChatColorMappingModel> {

  val isCustom: Boolean = chatColors.id is ChatColors.Id.Custom

  override fun areItemsTheSame(newItem: ChatColorMappingModel): Boolean {
    return chatColors == newItem.chatColors && isAuto == newItem.isAuto
  }

  override fun areContentsTheSame(newItem: ChatColorMappingModel): Boolean {
    return areItemsTheSame(newItem) && isSelected == newItem.isSelected
  }
}
