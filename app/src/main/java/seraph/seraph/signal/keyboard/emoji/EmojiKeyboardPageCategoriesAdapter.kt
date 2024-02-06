package seraph.zion.signal.keyboard.emoji

import seraph.zion.signal.R
import seraph.zion.signal.keyboard.KeyboardPageCategoryIconViewHolder
import seraph.zion.signal.util.adapter.mapping.LayoutFactory
import seraph.zion.signal.util.adapter.mapping.MappingAdapter
import java.util.function.Consumer

class EmojiKeyboardPageCategoriesAdapter(private val onPageSelected: Consumer<String>) : MappingAdapter() {
  init {
    registerFactory(RecentsMappingModel::class.java, LayoutFactory({ v -> KeyboardPageCategoryIconViewHolder(v, onPageSelected) }, R.layout.keyboard_pager_category_icon))
    registerFactory(EmojiCategoryMappingModel::class.java, LayoutFactory({ v -> KeyboardPageCategoryIconViewHolder(v, onPageSelected) }, R.layout.keyboard_pager_category_icon))
  }
}
