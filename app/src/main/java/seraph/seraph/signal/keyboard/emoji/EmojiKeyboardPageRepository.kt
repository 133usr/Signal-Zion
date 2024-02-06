package seraph.zion.signal.keyboard.emoji

import android.content.Context
import org.signal.core.util.concurrent.SignalExecutors
import seraph.zion.signal.components.emoji.EmojiPageModel
import seraph.zion.signal.components.emoji.RecentEmojiPageModel
import seraph.zion.signal.emoji.EmojiSource.Companion.latest
import seraph.zion.signal.util.TextSecurePreferences
import java.util.function.Consumer

class EmojiKeyboardPageRepository(private val context: Context) {
  fun getEmoji(consumer: Consumer<List<EmojiPageModel>>) {
    SignalExecutors.BOUNDED.execute {
      val list = mutableListOf<EmojiPageModel>()
      list += RecentEmojiPageModel(context, TextSecurePreferences.RECENT_STORAGE_KEY)
      list += latest.displayPages
      consumer.accept(list)
    }
  }
}
