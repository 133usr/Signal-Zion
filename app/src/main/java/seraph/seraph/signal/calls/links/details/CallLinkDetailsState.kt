/**
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.calls.links.details

import androidx.compose.runtime.Immutable
import seraph.zion.signal.database.CallLinkTable

@Immutable
data class CallLinkDetailsState(
  val displayRevocationDialog: Boolean = false,
  val callLink: CallLinkTable.CallLink? = null
)
