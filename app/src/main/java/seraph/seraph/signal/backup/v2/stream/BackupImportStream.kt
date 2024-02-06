/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.backup.v2.stream

import seraph.zion.signal.backup.v2.proto.Frame

interface BackupImportStream {
  fun read(): Frame?
}
