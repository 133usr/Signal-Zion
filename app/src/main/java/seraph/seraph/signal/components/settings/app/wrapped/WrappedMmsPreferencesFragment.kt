package seraph.zion.signal.components.settings.app.wrapped

import androidx.fragment.app.Fragment
import seraph.zion.signal.R
import seraph.zion.signal.preferences.MmsPreferencesFragment

class WrappedMmsPreferencesFragment : SettingsWrapperFragment() {
  override fun getFragment(): Fragment {
    toolbar.setTitle(R.string.preferences__advanced_mms_access_point_names)
    return MmsPreferencesFragment()
  }
}
