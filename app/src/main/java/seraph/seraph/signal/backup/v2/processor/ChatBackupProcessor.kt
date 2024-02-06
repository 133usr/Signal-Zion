/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.backup.v2.processor

import org.signal.core.util.logging.Log
import seraph.zion.signal.backup.v2.BackupState
import seraph.zion.signal.backup.v2.database.getThreadsForBackup
import seraph.zion.signal.backup.v2.database.restoreFromBackup
import seraph.zion.signal.backup.v2.proto.Chat
import seraph.zion.signal.backup.v2.proto.Frame
import seraph.zion.signal.backup.v2.stream.BackupFrameEmitter
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.recipients.RecipientId

object ChatBackupProcessor {
  val TAG = Log.tag(ChatBackupProcessor::class.java)

  fun export(emitter: BackupFrameEmitter) {
    SignalDatabase.threads.getThreadsForBackup().use { reader ->
      for (chat in reader) {
        emitter.emit(Frame(chat = chat))
      }
    }
  }

  fun import(chat: Chat, backupState: BackupState) {
    val recipientId: RecipientId? = backupState.backupToLocalRecipientId[chat.recipientId]
    if (recipientId == null) {
      Log.w(TAG, "Missing recipient for chat ${chat.id}")
      return
    }

    SignalDatabase.threads.restoreFromBackup(chat, recipientId)?.let { threadId ->
      backupState.chatIdToLocalRecipientId[chat.id] = recipientId
      backupState.chatIdToLocalThreadId[chat.id] = threadId
      backupState.chatIdToBackupRecipientId[chat.id] = chat.recipientId
    }

    // TODO there's several fields in the chat that actually need to be restored on the recipient table
  }
}
