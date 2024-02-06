package seraph.zion.signal.video.interfaces

fun interface TranscoderCancelationSignal {
  fun isCanceled(): Boolean
}
