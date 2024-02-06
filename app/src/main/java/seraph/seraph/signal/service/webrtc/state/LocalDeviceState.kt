package seraph.zion.signal.service.webrtc.state

import seraph.zion.signal.components.sensors.Orientation
import seraph.zion.signal.ringrtc.CameraState
import seraph.zion.signal.webrtc.audio.SignalAudioManager
import org.webrtc.PeerConnection

/**
 * Local device specific state.
 */
data class LocalDeviceState constructor(
  var cameraState: CameraState = CameraState.UNKNOWN,
  var isMicrophoneEnabled: Boolean = true,
  var orientation: Orientation = Orientation.PORTRAIT_BOTTOM_EDGE,
  var isLandscapeEnabled: Boolean = false,
  var deviceOrientation: Orientation = Orientation.PORTRAIT_BOTTOM_EDGE,
  var activeDevice: SignalAudioManager.AudioDevice = SignalAudioManager.AudioDevice.NONE,
  var availableDevices: Set<SignalAudioManager.AudioDevice> = emptySet(),
  var bluetoothPermissionDenied: Boolean = false,
  var networkConnectionType: PeerConnection.AdapterType = PeerConnection.AdapterType.UNKNOWN
) {

  fun duplicate(): LocalDeviceState {
    return copy()
  }
}
