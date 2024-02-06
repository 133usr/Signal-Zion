package seraph.zion.signal.components.settings.app.wrapped

import androidx.fragment.app.Fragment
import seraph.zion.signal.R
import seraph.zion.signal.preferences.AdvancedPinPreferenceFragment

class WrappedAdvancedPinPreferenceFragment : SettingsWrapperFragment() {
  override fun getFragment(): Fragment {
    toolbar.setTitle(R.string.preferences__advanced_pin_settings)
    return AdvancedPinPreferenceFragment()
  }
}
