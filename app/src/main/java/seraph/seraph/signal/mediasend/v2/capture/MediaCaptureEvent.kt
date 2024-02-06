package seraph.zion.signal.mediasend.v2.capture

import seraph.zion.signal.mediasend.Media

sealed class MediaCaptureEvent {
  data class MediaCaptureRendered(val media: Media) : MediaCaptureEvent()
  object MediaCaptureRenderFailed : MediaCaptureEvent()
}
