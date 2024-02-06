package seraph.zion.signal.keyboard.emoji

import seraph.zion.signal.components.emoji.EmojiEventListener
import seraph.zion.signal.keyboard.emoji.search.EmojiSearchFragment

interface EmojiKeyboardCallback :
  EmojiEventListener,
  EmojiKeyboardPageFragment.Callback,
  EmojiSearchFragment.Callback
