package seraph.zion.signal.migrations

import seraph.zion.signal.database.SignalDatabase.Companion.recipients
import seraph.zion.signal.jobmanager.Job
import seraph.zion.signal.keyvalue.SignalStore
import seraph.zion.signal.recipients.Recipient
import seraph.zion.signal.storage.StorageSyncHelper
import seraph.zion.signal.util.TextSecurePreferences

/**
 * Added as a way to initialize the story viewed receipts setting.
 */
internal class StoryViewedReceiptsStateMigrationJob(
  parameters: Parameters = Parameters.Builder().build()
) : MigrationJob(parameters) {
  companion object {
    const val KEY = "StoryViewedReceiptsStateMigrationJob"
  }

  override fun getFactoryKey(): String = KEY

  override fun isUiBlocking(): Boolean = false

  override fun performMigration() {
    if (!SignalStore.storyValues().isViewedReceiptsStateSet()) {
      SignalStore.storyValues().viewedReceiptsEnabled = TextSecurePreferences.isReadReceiptsEnabled(context)
      if (SignalStore.account().isRegistered) {
        recipients.markNeedsSync(Recipient.self().id)
        StorageSyncHelper.scheduleSyncForDataChange()
      }
    }
  }

  override fun shouldRetry(e: Exception): Boolean = false

  class Factory : Job.Factory<StoryViewedReceiptsStateMigrationJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): StoryViewedReceiptsStateMigrationJob {
      return StoryViewedReceiptsStateMigrationJob(parameters)
    }
  }
}
