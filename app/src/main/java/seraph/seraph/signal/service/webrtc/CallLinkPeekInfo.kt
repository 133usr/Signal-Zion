/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.service.webrtc

import org.signal.ringrtc.CallId
import org.signal.ringrtc.PeekInfo

/**
 * App-level peek info object for call links.
 */
data class CallLinkPeekInfo(
  val callId: CallId?
) {
  companion object {
    @JvmStatic
    fun fromPeekInfo(peekInfo: PeekInfo): CallLinkPeekInfo {
      return CallLinkPeekInfo(
        callId = peekInfo.eraId?.let { CallId.fromEra(it) }
      )
    }
  }
}
