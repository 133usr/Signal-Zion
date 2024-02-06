package seraph.zion.signal.components.webrtc.participantslist;

import seraph.zion.signal.R;
import seraph.zion.signal.util.adapter.mapping.LayoutFactory;
import seraph.zion.signal.util.adapter.mapping.MappingAdapter;

public class CallParticipantsListAdapter extends MappingAdapter {

  CallParticipantsListAdapter() {
    registerFactory(CallParticipantsListHeader.class, new LayoutFactory<>(CallParticipantsListHeaderViewHolder::new, R.layout.call_participants_list_header));
    registerFactory(CallParticipantViewState.class, new LayoutFactory<>(CallParticipantViewHolder::new, R.layout.call_participants_list_item));
  }

}
