package seraph.zion.signal.components.settings.app.wrapped

import androidx.fragment.app.Fragment
import seraph.zion.signal.preferences.StoragePreferenceFragment

class WrappedStoragePreferenceFragment : SettingsWrapperFragment() {
  override fun getFragment(): Fragment {
    return StoragePreferenceFragment()
  }
}
