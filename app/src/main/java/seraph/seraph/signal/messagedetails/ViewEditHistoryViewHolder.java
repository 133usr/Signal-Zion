package seraph.zion.signal.messagedetails;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import seraph.zion.signal.database.model.MessageRecord;
import seraph.zion.signal.databinding.MessageDetailsViewEditHistoryBinding;

public class ViewEditHistoryViewHolder extends RecyclerView.ViewHolder {

  private final seraph.zion.signal.databinding.MessageDetailsViewEditHistoryBinding binding;
  private final MessageDetailsAdapter.Callbacks                                             callbacks;

  public ViewEditHistoryViewHolder(@NonNull MessageDetailsViewEditHistoryBinding binding, @NonNull MessageDetailsAdapter.Callbacks callbacks) {
    super(binding.getRoot());
    this.binding   = binding;
    this.callbacks = callbacks;
  }

  public void bind(@NonNull MessageRecord record) {
    binding.viewEditHistory.setOnClickListener(v -> callbacks.onViewEditHistoryClicked(record));
  }
}
