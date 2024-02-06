package seraph.zion.signal.scribbles;

import androidx.annotation.NonNull;

import org.signal.imageeditor.core.HiddenEditText;
import seraph.zion.signal.components.emoji.EmojiUtil;

class RemoveEmojiTextFilter implements HiddenEditText.TextFilter {
  @Override
  public String filter(@NonNull String text) {
    return EmojiUtil.stripEmoji(text);
  }
}
