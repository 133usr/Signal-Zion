package seraph.zion.signal.main

import android.widget.ImageView
import seraph.zion.signal.components.Material3SearchToolbar
import seraph.zion.signal.util.views.Stub

interface SearchBinder {
  fun getSearchAction(): ImageView

  fun getSearchToolbar(): Stub<Material3SearchToolbar>

  fun onSearchOpened()

  fun onSearchClosed()
}
