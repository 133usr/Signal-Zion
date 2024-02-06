package seraph.zion.signal.util

import android.content.Context
import android.content.pm.PackageManager
import org.signal.core.util.logging.Log
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.jobs.RefreshAttributesJob
import seraph.zion.signal.jobs.RemoteConfigRefreshJob
import seraph.zion.signal.jobs.RetrieveRemoteAnnouncementsJob
import seraph.zion.signal.keyvalue.SignalStore
import java.time.Duration

object VersionTracker {
  private val TAG = Log.tag(VersionTracker::class.java)

  @JvmStatic
  fun getLastSeenVersion(context: Context): Int {
    return TextSecurePreferences.getLastVersionCode(context)
  }

  @JvmStatic
  fun updateLastSeenVersion(context: Context) {
    val currentVersionCode = Util.getCanonicalVersionCode()
    val lastVersionCode = TextSecurePreferences.getLastVersionCode(context)

    if (currentVersionCode != lastVersionCode) {
      Log.i(TAG, "Upgraded from $lastVersionCode to $currentVersionCode")
      SignalStore.misc().clearClientDeprecated()
      val jobChain = listOf(RemoteConfigRefreshJob(), RefreshAttributesJob())
      ApplicationDependencies.getJobManager().startChain(jobChain).enqueue()
      RetrieveRemoteAnnouncementsJob.enqueue(true)
      LocalMetrics.getInstance().clear()
    }

    TextSecurePreferences.setLastVersionCode(context, currentVersionCode)
  }

  @JvmStatic
  fun getDaysSinceFirstInstalled(context: Context): Long {
    return try {
      val installTimestamp = context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
      Duration.ofMillis(System.currentTimeMillis() - installTimestamp).toDays()
    } catch (e: PackageManager.NameNotFoundException) {
      Log.w(TAG, e)
      0
    }
  }
}
