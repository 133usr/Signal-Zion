package seraph.zion.signal.video.videoconverter.utils

object Preconditions {
  @JvmStatic
  fun checkState(errorMessage: String, expression: Boolean) {
    check(expression) { errorMessage }
  }
}
