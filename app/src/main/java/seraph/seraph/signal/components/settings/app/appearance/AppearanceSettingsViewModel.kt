package seraph.zion.signal.components.settings.app.appearance

import android.app.Activity
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.core.Flowable
import seraph.zion.signal.jobs.EmojiSearchIndexDownloadJob
import seraph.zion.signal.keyvalue.SettingsValues.Theme
import seraph.zion.signal.keyvalue.SignalStore
import seraph.zion.signal.util.SplashScreenUtil
import seraph.zion.signal.util.rx.RxStore

class AppearanceSettingsViewModel : ViewModel() {
  private val store = RxStore(getState())
  val state: Flowable<AppearanceSettingsState> = store.stateFlowable

  override fun onCleared() {
    super.onCleared()
    store.dispose()
  }

  fun refreshState() {
    store.update { getState() }
  }

  fun setTheme(activity: Activity?, theme: Theme) {
    store.update { it.copy(theme = theme) }
    SignalStore.settings().theme = theme
    SplashScreenUtil.setSplashScreenThemeIfNecessary(activity, theme)
  }

  fun setLanguage(language: String) {
    store.update { it.copy(language = language) }
    SignalStore.settings().language = language
    EmojiSearchIndexDownloadJob.scheduleImmediately()
  }

  fun setMessageFontSize(size: Int) {
    store.update { it.copy(messageFontSize = size) }
    SignalStore.settings().messageFontSize = size
  }

  private fun getState(): AppearanceSettingsState {
    return AppearanceSettingsState(
      SignalStore.settings().theme,
      SignalStore.settings().messageFontSize,
      SignalStore.settings().language,
      SignalStore.settings().useCompactNavigationBar
    )
  }
}
