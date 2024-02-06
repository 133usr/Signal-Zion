/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.backup.v2.database

import okio.ByteString.Companion.toByteString
import org.signal.core.util.CursorUtil
import org.signal.core.util.deleteAll
import org.signal.core.util.logging.Log
import org.signal.core.util.readToList
import org.signal.core.util.requireLong
import org.signal.core.util.requireNonNullString
import org.signal.core.util.requireObject
import org.signal.core.util.select
import seraph.zion.signal.backup.v2.BackupState
import seraph.zion.signal.database.DistributionListTables
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.database.model.DistributionListId
import seraph.zion.signal.database.model.DistributionListPrivacyMode
import seraph.zion.signal.database.model.DistributionListRecord
import seraph.zion.signal.recipients.RecipientId
import org.whispersystems.signalservice.api.push.DistributionId
import org.whispersystems.signalservice.api.util.UuidUtil
import org.whispersystems.signalservice.api.util.toByteArray
import seraph.zion.signal.backup.v2.proto.DistributionList as BackupDistributionList

private val TAG = Log.tag(DistributionListTables::class.java)

fun DistributionListTables.getAllForBackup(): List<BackupRecipient> {
  val records = readableDatabase
    .select()
    .from(DistributionListTables.ListTable.TABLE_NAME)
    .run()
    .readToList { cursor ->
      val id: DistributionListId = DistributionListId.from(cursor.requireLong(DistributionListTables.ListTable.ID))
      val privacyMode: DistributionListPrivacyMode = cursor.requireObject(DistributionListTables.ListTable.PRIVACY_MODE, DistributionListPrivacyMode.Serializer)

      DistributionListRecord(
        id = id,
        name = cursor.requireNonNullString(DistributionListTables.ListTable.NAME),
        distributionId = DistributionId.from(cursor.requireNonNullString(DistributionListTables.ListTable.DISTRIBUTION_ID)),
        allowsReplies = CursorUtil.requireBoolean(cursor, DistributionListTables.ListTable.ALLOWS_REPLIES),
        rawMembers = getRawMembers(id, privacyMode),
        members = getMembers(id),
        deletedAtTimestamp = 0L,
        isUnknown = CursorUtil.requireBoolean(cursor, DistributionListTables.ListTable.IS_UNKNOWN),
        privacyMode = privacyMode
      )
    }

  return records
    .map { record ->
      BackupRecipient(
        distributionList = BackupDistributionList(
          name = record.name,
          distributionId = record.distributionId.asUuid().toByteArray().toByteString(),
          allowReplies = record.allowsReplies,
          deletionTimestamp = record.deletedAtTimestamp,
          privacyMode = record.privacyMode.toBackupPrivacyMode(),
          memberRecipientIds = record.members.map { it.toLong() }
        )
      )
    }
}

fun DistributionListTables.restoreFromBackup(dlist: BackupDistributionList, backupState: BackupState): RecipientId {
  val members: List<RecipientId> = dlist.memberRecipientIds
    .mapNotNull { backupState.backupToLocalRecipientId[it] }

  if (members.size != dlist.memberRecipientIds.size) {
    Log.w(TAG, "Couldn't find some member recipients! Missing backup recipientIds: ${dlist.memberRecipientIds.toSet() - members.toSet()}")
  }

  val dlistId = this.createList(
    name = dlist.name,
    members = members,
    distributionId = DistributionId.from(UuidUtil.fromByteString(dlist.distributionId)),
    allowsReplies = dlist.allowReplies,
    deletionTimestamp = dlist.deletionTimestamp,
    storageId = null,
    privacyMode = dlist.privacyMode.toLocalPrivacyMode()
  )!!

  return SignalDatabase.distributionLists.getRecipientId(dlistId)!!
}

fun DistributionListTables.clearAllDataForBackupRestore() {
  writableDatabase
    .deleteAll(DistributionListTables.ListTable.TABLE_NAME)

  writableDatabase
    .deleteAll(DistributionListTables.MembershipTable.TABLE_NAME)
}

private fun DistributionListPrivacyMode.toBackupPrivacyMode(): BackupDistributionList.PrivacyMode {
  return when (this) {
    DistributionListPrivacyMode.ONLY_WITH -> BackupDistributionList.PrivacyMode.ONLY_WITH
    DistributionListPrivacyMode.ALL -> BackupDistributionList.PrivacyMode.ALL
    DistributionListPrivacyMode.ALL_EXCEPT -> BackupDistributionList.PrivacyMode.ALL_EXCEPT
  }
}

private fun BackupDistributionList.PrivacyMode.toLocalPrivacyMode(): DistributionListPrivacyMode {
  return when (this) {
    BackupDistributionList.PrivacyMode.UNKNOWN -> DistributionListPrivacyMode.ALL
    BackupDistributionList.PrivacyMode.ONLY_WITH -> DistributionListPrivacyMode.ONLY_WITH
    BackupDistributionList.PrivacyMode.ALL -> DistributionListPrivacyMode.ALL
    BackupDistributionList.PrivacyMode.ALL_EXCEPT -> DistributionListPrivacyMode.ALL_EXCEPT
  }
}
