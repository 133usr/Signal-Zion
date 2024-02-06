/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.components.settings.app.updates

import android.os.Build
import seraph.zion.signal.R
import seraph.zion.signal.components.settings.DSLConfiguration
import seraph.zion.signal.components.settings.DSLSettingsFragment
import seraph.zion.signal.components.settings.DSLSettingsText
import seraph.zion.signal.components.settings.configure
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.jobs.ApkUpdateJob
import seraph.zion.signal.keyvalue.SignalStore
import seraph.zion.signal.util.adapter.mapping.MappingAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Settings around app updates. Only shown for builds that manage their own app updates.
 */
class AppUpdatesSettingsFragment : DSLSettingsFragment(R.string.preferences_app_updates__title) {

  override fun bindAdapter(adapter: MappingAdapter) {
    adapter.submitList(getConfiguration().toMappingModelList())
  }

  private fun getConfiguration(): DSLConfiguration {
    return configure {
      if (Build.VERSION.SDK_INT >= 31) {
        switchPref(
          title = DSLSettingsText.from("Automatic updates"),
          summary = DSLSettingsText.from("Automatically download and install app updates"),
          isChecked = SignalStore.apkUpdate().autoUpdate,
          onClick = {
            SignalStore.apkUpdate().autoUpdate = !SignalStore.apkUpdate().autoUpdate
          }
        )
      }

      clickPref(
        title = DSLSettingsText.from("Check for updates"),
        summary = DSLSettingsText.from("Last checked on: $lastSuccessfulUpdateString"),
        onClick = {
          ApplicationDependencies.getJobManager().add(ApkUpdateJob())
        }
      )
    }
  }

  private val lastSuccessfulUpdateString: String
    get() {
      val lastUpdateTime = SignalStore.apkUpdate().lastSuccessfulCheck

      return if (lastUpdateTime > 0) {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' h:mma", Locale.US)
        dateFormat.format(Date(lastUpdateTime))
      } else {
        "Never"
      }
    }
}
