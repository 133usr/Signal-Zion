/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.backup.v2.processor

import org.signal.core.util.logging.Log
import seraph.zion.signal.backup.v2.BackupState
import seraph.zion.signal.backup.v2.database.getCallsForBackup
import seraph.zion.signal.backup.v2.database.restoreCallLogFromBackup
import seraph.zion.signal.backup.v2.proto.Frame
import seraph.zion.signal.backup.v2.stream.BackupFrameEmitter
import seraph.zion.signal.database.SignalDatabase

typealias BackupCall = seraph.zion.signal.backup.v2.proto.Call

object CallLogBackupProcessor {

  val TAG = Log.tag(CallLogBackupProcessor::class.java)

  fun export(emitter: BackupFrameEmitter) {
    SignalDatabase.calls.getCallsForBackup().use { reader ->
      for (callLog in reader) {
        if (callLog != null) {
          emitter.emit(Frame(call = callLog))
        }
      }
    }
  }

  fun import(call: BackupCall, backupState: BackupState) {
    SignalDatabase.calls.restoreCallLogFromBackup(call, backupState)
  }
}
