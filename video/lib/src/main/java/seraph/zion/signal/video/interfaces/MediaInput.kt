/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.video.interfaces

import android.media.MediaExtractor
import java.io.Closeable
import java.io.IOException

/**
 * Abstraction over the different sources of media input for transcoding.
 */
interface MediaInput : Closeable {
  @Throws(IOException::class)
  fun createExtractor(): MediaExtractor
}
