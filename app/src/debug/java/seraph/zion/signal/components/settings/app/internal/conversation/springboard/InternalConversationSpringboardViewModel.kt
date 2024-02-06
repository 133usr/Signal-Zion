/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.components.settings.app.internal.conversation.springboard

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class InternalConversationSpringboardViewModel : ViewModel() {
  val hasWallpaper = mutableStateOf(false)
}
