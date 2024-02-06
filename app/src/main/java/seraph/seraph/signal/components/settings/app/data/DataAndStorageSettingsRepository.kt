package seraph.zion.signal.components.settings.app.data

import android.content.Context
import org.signal.core.util.concurrent.SignalExecutors
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.dependencies.ApplicationDependencies

class DataAndStorageSettingsRepository {

  private val context: Context = ApplicationDependencies.getApplication()

  fun getTotalStorageUse(consumer: (Long) -> Unit) {
    SignalExecutors.BOUNDED.execute {
      val breakdown = SignalDatabase.media.getStorageBreakdown()

      consumer(listOf(breakdown.audioSize, breakdown.documentSize, breakdown.photoSize, breakdown.videoSize).sum())
    }
  }
}
