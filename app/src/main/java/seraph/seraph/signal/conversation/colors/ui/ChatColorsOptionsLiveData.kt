package seraph.zion.signal.conversation.colors.ui

import androidx.lifecycle.LiveData
import org.signal.core.util.concurrent.SignalExecutors
import seraph.zion.signal.conversation.colors.ChatColors
import seraph.zion.signal.conversation.colors.ChatColorsPalette
import seraph.zion.signal.database.ChatColorsTable
import seraph.zion.signal.database.DatabaseObserver
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.util.concurrent.SerialMonoLifoExecutor
import java.util.concurrent.Executor

class ChatColorsOptionsLiveData : LiveData<List<ChatColors>>() {
  private val chatColorsTable: ChatColorsTable = SignalDatabase.chatColors
  private val observer: DatabaseObserver.Observer = DatabaseObserver.Observer { refreshChatColors() }
  private val executor: Executor = SerialMonoLifoExecutor(SignalExecutors.BOUNDED)

  override fun onActive() {
    refreshChatColors()
    ApplicationDependencies.getDatabaseObserver().registerChatColorsObserver(observer)
  }

  override fun onInactive() {
    ApplicationDependencies.getDatabaseObserver().unregisterObserver(observer)
  }

  private fun refreshChatColors() {
    executor.execute {
      val options = mutableListOf<ChatColors>().apply {
        addAll(ChatColorsPalette.Bubbles.all)
        addAll(chatColorsTable.getSavedChatColors())
      }

      postValue(options)
    }
  }
}
