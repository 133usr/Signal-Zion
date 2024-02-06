/**
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.service.webrtc.links

/**
 * Result type for call link updates.
 */
sealed interface UpdateCallLinkResult {
  data class Success(
    val state: SignalCallLinkState
  ) : UpdateCallLinkResult

  data class Failure(
    val status: Short
  ) : UpdateCallLinkResult

  object NotAuthorized : UpdateCallLinkResult
}
