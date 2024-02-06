package seraph.zion.signal.components.settings.app.chats

import android.content.Context
import org.signal.core.util.concurrent.SignalExecutors
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.jobs.MultiDeviceConfigurationUpdateJob
import seraph.zion.signal.jobs.MultiDeviceContactUpdateJob
import seraph.zion.signal.keyvalue.SignalStore
import seraph.zion.signal.recipients.Recipient
import seraph.zion.signal.storage.StorageSyncHelper
import seraph.zion.signal.util.TextSecurePreferences

class ChatsSettingsRepository {

  private val context: Context = ApplicationDependencies.getApplication()

  fun syncLinkPreviewsState() {
    SignalExecutors.BOUNDED.execute {
      val isLinkPreviewsEnabled = SignalStore.settings().isLinkPreviewsEnabled

      SignalDatabase.recipients.markNeedsSync(Recipient.self().id)
      StorageSyncHelper.scheduleSyncForDataChange()
      ApplicationDependencies.getJobManager().add(
        MultiDeviceConfigurationUpdateJob(
          TextSecurePreferences.isReadReceiptsEnabled(context),
          TextSecurePreferences.isTypingIndicatorsEnabled(context),
          TextSecurePreferences.isShowUnidentifiedDeliveryIndicatorsEnabled(context),
          isLinkPreviewsEnabled
        )
      )
    }
  }

  fun syncPreferSystemContactPhotos() {
    SignalExecutors.BOUNDED.execute {
      SignalDatabase.recipients.markNeedsSync(Recipient.self().id)
      ApplicationDependencies.getJobManager().add(MultiDeviceContactUpdateJob(true))
      StorageSyncHelper.scheduleSyncForDataChange()
    }
  }

  fun syncKeepMutedChatsArchivedState() {
    SignalExecutors.BOUNDED.execute {
      SignalDatabase.recipients.markNeedsSync(Recipient.self().id)
      StorageSyncHelper.scheduleSyncForDataChange()
    }
  }
}
