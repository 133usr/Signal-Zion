/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.backup.v2.processor

import org.signal.core.util.logging.Log
import seraph.zion.signal.backup.v2.BackupState
import seraph.zion.signal.backup.v2.database.getAllForBackup
import seraph.zion.signal.backup.v2.database.getContactsForBackup
import seraph.zion.signal.backup.v2.database.getGroupsForBackup
import seraph.zion.signal.backup.v2.database.restoreRecipientFromBackup
import seraph.zion.signal.backup.v2.proto.Frame
import seraph.zion.signal.backup.v2.stream.BackupFrameEmitter
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.recipients.Recipient

typealias BackupRecipient = seraph.zion.signal.backup.v2.proto.Recipient

object RecipientBackupProcessor {

  val TAG = Log.tag(RecipientBackupProcessor::class.java)

  fun export(emitter: BackupFrameEmitter) {
    val selfId = Recipient.self().id.toLong()

    SignalDatabase.recipients.getContactsForBackup(selfId).use { reader ->
      for (backupRecipient in reader) {
        if (backupRecipient != null) {
          emitter.emit(Frame(recipient = backupRecipient))
        }
      }
    }

    SignalDatabase.recipients.getGroupsForBackup().use { reader ->
      for (backupRecipient in reader) {
        emitter.emit(Frame(recipient = backupRecipient))
      }
    }

    SignalDatabase.distributionLists.getAllForBackup().forEach {
      emitter.emit(Frame(recipient = it))
    }
  }

  fun import(recipient: BackupRecipient, backupState: BackupState) {
    val newId = SignalDatabase.recipients.restoreRecipientFromBackup(recipient, backupState)
    if (newId != null) {
      backupState.backupToLocalRecipientId[recipient.id] = newId
    }
  }
}
