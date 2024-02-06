package seraph.zion.signal.components.settings.app.wrapped

import androidx.fragment.app.Fragment
import seraph.zion.signal.R
import seraph.zion.signal.help.HelpFragment

class WrappedHelpFragment : SettingsWrapperFragment() {
  override fun getFragment(): Fragment {
    toolbar.title = getString(R.string.preferences__help)

    val fragment = HelpFragment()
    fragment.arguments = arguments

    return fragment
  }
}
