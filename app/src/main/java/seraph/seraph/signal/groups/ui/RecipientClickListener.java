package seraph.zion.signal.groups.ui;

import androidx.annotation.NonNull;

import seraph.zion.signal.recipients.Recipient;

public interface RecipientClickListener {
  void onClick(@NonNull Recipient recipient);
}
