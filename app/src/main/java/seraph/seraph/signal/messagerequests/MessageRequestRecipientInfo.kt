/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package seraph.zion.signal.messagerequests

import seraph.zion.signal.recipients.Recipient

/**
 * Thread recipient and message request state information necessary to render
 * a thread header.
 */
data class MessageRequestRecipientInfo(
  val recipient: Recipient,
  val groupInfo: GroupInfo = GroupInfo.ZERO,
  val sharedGroups: List<String> = emptyList(),
  val messageRequestState: MessageRequestState? = null
)
