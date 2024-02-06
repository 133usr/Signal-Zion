/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.conversation.v2.groups

import seraph.zion.signal.recipients.RecipientId

/** State of a group call used solely within rendering UX/UI in the conversation */
data class ConversationGroupCallState(
  val recipientId: RecipientId? = null,
  val activeV2Group: Boolean = false,
  val ongoingCall: Boolean = false,
  val hasCapacity: Boolean = false
)
