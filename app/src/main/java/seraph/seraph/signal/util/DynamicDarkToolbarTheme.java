package seraph.zion.signal.util;

import androidx.annotation.StyleRes;

import seraph.zion.signal.R;

public class DynamicDarkToolbarTheme extends DynamicTheme {

  protected @StyleRes int getTheme() {
    return R.style.Signal_DayNight_DarkNoActionBar;
  }
}
