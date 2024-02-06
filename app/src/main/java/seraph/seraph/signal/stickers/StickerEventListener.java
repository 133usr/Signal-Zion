package seraph.zion.signal.stickers;

import androidx.annotation.NonNull;

import seraph.zion.signal.database.model.StickerRecord;

public interface StickerEventListener {
  void onStickerSelected(@NonNull StickerRecord sticker);

  void onStickerManagementClicked();
}
