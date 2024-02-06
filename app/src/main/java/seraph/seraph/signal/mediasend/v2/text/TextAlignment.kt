package seraph.zion.signal.mediasend.v2.text

import android.view.Gravity
import androidx.annotation.DrawableRes
import seraph.zion.signal.R

enum class TextAlignment(val gravity: Int, @DrawableRes val icon: Int) {
  START(Gravity.START or Gravity.CENTER_VERTICAL, R.drawable.ic_text_start),
  CENTER(Gravity.CENTER, R.drawable.ic_text_center),
  END(Gravity.END or Gravity.CENTER_VERTICAL, R.drawable.ic_text_end)
}
