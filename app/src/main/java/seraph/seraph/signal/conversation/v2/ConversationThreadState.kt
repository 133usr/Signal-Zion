/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.conversation.v2

import org.signal.paging.ObservablePagedData
import seraph.zion.signal.conversation.ConversationData
import seraph.zion.signal.conversation.v2.data.ConversationElementKey
import seraph.zion.signal.util.adapter.mapping.MappingModel

/**
 * Represents the content that will be displayed in the conversation
 * thread (recycler).
 */
class ConversationThreadState(
  val items: ObservablePagedData<ConversationElementKey, MappingModel<*>>,
  val meta: ConversationData
)
