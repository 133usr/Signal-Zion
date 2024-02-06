package seraph.zion.signal.components.settings.app.wrapped

import androidx.fragment.app.Fragment
import seraph.zion.signal.R
import seraph.zion.signal.delete.DeleteAccountFragment

class WrappedDeleteAccountFragment : SettingsWrapperFragment() {
  override fun getFragment(): Fragment {
    toolbar.setTitle(R.string.preferences__delete_account)
    return DeleteAccountFragment()
  }
}
