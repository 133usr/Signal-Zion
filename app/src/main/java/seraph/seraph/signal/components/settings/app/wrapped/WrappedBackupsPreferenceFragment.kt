package seraph.zion.signal.components.settings.app.wrapped

import androidx.fragment.app.Fragment
import seraph.zion.signal.R
import seraph.zion.signal.preferences.BackupsPreferenceFragment

class WrappedBackupsPreferenceFragment : SettingsWrapperFragment() {
  override fun getFragment(): Fragment {
    toolbar.setTitle(R.string.BackupsPreferenceFragment__chat_backups)
    return BackupsPreferenceFragment()
  }
}
