package seraph.zion.signal.components.webrtc

import androidx.annotation.RequiresApi

@RequiresApi(31)
interface OnAudioOutputChangedListener31 {
  fun audioOutputChanged(audioDeviceId: Int)
}
