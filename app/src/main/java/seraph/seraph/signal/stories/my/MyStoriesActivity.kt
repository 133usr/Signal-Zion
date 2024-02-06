package seraph.zion.signal.stories.my

import androidx.fragment.app.Fragment
import seraph.zion.signal.components.FragmentWrapperActivity

class MyStoriesActivity : FragmentWrapperActivity() {
  override fun getFragment(): Fragment {
    return MyStoriesFragment()
  }
}
