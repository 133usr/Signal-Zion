/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.conversation.v2.computed

data class FormattedDate(
  val isRelative: Boolean,
  val isNow: Boolean,
  val value: String
)
