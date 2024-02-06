package seraph.zion.signal.profiles.username

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import seraph.zion.signal.BaseActivity
import seraph.zion.signal.R
import seraph.zion.signal.profiles.manage.UsernameEditFragmentArgs
import seraph.zion.signal.profiles.manage.UsernameEditMode
import seraph.zion.signal.util.DynamicNoActionBarTheme
import seraph.zion.signal.util.DynamicTheme

class AddAUsernameActivity : BaseActivity() {
  private val dynamicTheme: DynamicTheme = DynamicNoActionBarTheme()
  private val contentViewId: Int = R.layout.fragment_container

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(contentViewId)
    dynamicTheme.onCreate(this)

    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .replace(
          R.id.fragment_container,
          NavHostFragment.create(
            R.navigation.create_username,
            UsernameEditFragmentArgs.Builder().setMode(UsernameEditMode.REGISTRATION).build().toBundle()
          )
        )
        .commit()
    }
  }

  override fun onResume() {
    super.onResume()
    dynamicTheme.onResume(this)
  }
}
