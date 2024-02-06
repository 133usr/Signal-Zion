package seraph.zion.signal.conversation.ui.mentions;

import androidx.annotation.NonNull;

import seraph.zion.signal.recipients.Recipient;
import seraph.zion.signal.util.viewholders.RecipientMappingModel;

public final class MentionViewState extends RecipientMappingModel<MentionViewState> {

  private final Recipient recipient;

  public MentionViewState(@NonNull Recipient recipient) {
    this.recipient = recipient;
  }

  @Override
  public @NonNull Recipient getRecipient() {
    return recipient;
  }
}
