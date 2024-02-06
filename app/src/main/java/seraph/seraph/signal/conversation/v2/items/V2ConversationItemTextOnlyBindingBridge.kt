/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.conversation.v2.items

import android.view.View
import android.view.ViewGroup
import android.widget.Space
import android.widget.TextView
import com.google.android.material.imageview.ShapeableImageView
import seraph.zion.signal.badges.BadgeImageView
import seraph.zion.signal.components.AlertView
import seraph.zion.signal.components.AvatarImageView
import seraph.zion.signal.components.DeliveryStatusView
import seraph.zion.signal.components.ExpirationTimerView
import seraph.zion.signal.components.emoji.EmojiTextView
import seraph.zion.signal.databinding.V2ConversationItemTextOnlyIncomingBinding
import seraph.zion.signal.databinding.V2ConversationItemTextOnlyOutgoingBinding
import seraph.zion.signal.reactions.ReactionsConversationView

/**
 * Pass-through interface for bridging incoming and outgoing text-only message views.
 *
 * Essentially, just a convenience wrapper since the layouts differ *very slightly* and
 * we want to be able to have each follow the same code-path.
 */
data class V2ConversationItemTextOnlyBindingBridge(
  val root: V2ConversationItemLayout,
  val senderName: EmojiTextView?,
  val senderPhoto: AvatarImageView?,
  val senderBadge: BadgeImageView?,
  val bodyWrapper: ViewGroup,
  val body: EmojiTextView,
  val reply: ShapeableImageView,
  val reactions: ReactionsConversationView,
  val deliveryStatus: DeliveryStatusView?,
  val footerDate: TextView,
  val footerExpiry: ExpirationTimerView,
  val footerBackground: View,
  val footerSpace: Space?,
  val alert: AlertView?,
  val isIncoming: Boolean
)

/**
 * Wraps the binding in the bridge.
 */
fun V2ConversationItemTextOnlyIncomingBinding.bridge(): V2ConversationItemTextOnlyBindingBridge {
  return V2ConversationItemTextOnlyBindingBridge(
    root = root,
    senderName = groupMessageSender,
    senderPhoto = contactPhoto,
    senderBadge = badge,
    body = conversationItemBody,
    bodyWrapper = conversationItemBodyWrapper,
    reply = conversationItemReply,
    reactions = conversationItemReactions,
    deliveryStatus = null,
    footerDate = conversationItemFooterDate,
    footerExpiry = conversationItemExpirationTimer,
    footerBackground = conversationItemFooterBackground,
    alert = null,
    footerSpace = footerEndPad,
    isIncoming = true
  )
}

/**
 * Wraps the binding in the bridge.
 */
fun V2ConversationItemTextOnlyOutgoingBinding.bridge(): V2ConversationItemTextOnlyBindingBridge {
  return V2ConversationItemTextOnlyBindingBridge(
    root = root,
    senderName = null,
    senderPhoto = null,
    senderBadge = null,
    body = conversationItemBody,
    bodyWrapper = conversationItemBodyWrapper,
    reply = conversationItemReply,
    reactions = conversationItemReactions,
    deliveryStatus = conversationItemDeliveryStatus,
    footerDate = conversationItemFooterDate,
    footerExpiry = conversationItemExpirationTimer,
    footerBackground = conversationItemFooterBackground,
    alert = conversationItemAlert,
    footerSpace = footerEndPad,
    isIncoming = false
  )
}
