package seraph.zion.signal.webrtc

import seraph.zion.signal.components.webrtc.CallParticipantsState
import seraph.zion.signal.service.webrtc.state.WebRtcEphemeralState

class CallParticipantsViewState(
  callParticipantsState: CallParticipantsState,
  ephemeralState: WebRtcEphemeralState,
  val isPortrait: Boolean,
  val isLandscapeEnabled: Boolean,
  val isStartedFromCallLink: Boolean
) {

  val callParticipantsState = CallParticipantsState.update(callParticipantsState, ephemeralState)
}
