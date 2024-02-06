/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.conversation.v2.items

import com.bumptech.glide.RequestManager
import seraph.zion.signal.conversation.ConversationAdapter
import seraph.zion.signal.conversation.ConversationItemDisplayMode
import seraph.zion.signal.conversation.colors.Colorizer
import seraph.zion.signal.conversation.mutiselect.MultiselectPart
import seraph.zion.signal.database.model.MessageRecord

/**
 * Describes the Adapter "context" that would normally have been
 * visible to an inner class.
 */
interface V2ConversationContext {
  val requestManager: RequestManager
  val displayMode: ConversationItemDisplayMode
  val clickListener: ConversationAdapter.ItemClickListener
  val selectedItems: Set<MultiselectPart>
  val isMessageRequestAccepted: Boolean
  val searchQuery: String?
  val isParentInScroll: Boolean

  fun getChatColorsData(): ChatColorsDrawable.ChatColorsData

  fun onStartExpirationTimeout(messageRecord: MessageRecord)

  fun hasWallpaper(): Boolean
  fun getColorizer(): Colorizer
  fun getNextMessage(adapterPosition: Int): MessageRecord?
  fun getPreviousMessage(adapterPosition: Int): MessageRecord?
}
