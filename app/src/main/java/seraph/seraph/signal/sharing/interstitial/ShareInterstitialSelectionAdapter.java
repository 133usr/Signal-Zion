package seraph.zion.signal.sharing.interstitial;

import seraph.zion.signal.R;
import seraph.zion.signal.util.adapter.mapping.MappingAdapter;
import seraph.zion.signal.util.viewholders.RecipientViewHolder;

class ShareInterstitialSelectionAdapter extends MappingAdapter {
  ShareInterstitialSelectionAdapter() {
    registerFactory(ShareInterstitialMappingModel.class, RecipientViewHolder.createFactory(R.layout.share_contact_selection_item, null));
  }
}
