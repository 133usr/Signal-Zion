/**
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.service.webrtc.links

/**
 * Result type for call link reads.
 */
sealed interface ReadCallLinkResult {
  data class Success(
    val callLinkState: SignalCallLinkState
  ) : ReadCallLinkResult

  data class Failure(val status: Short) : ReadCallLinkResult
}
