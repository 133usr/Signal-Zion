package seraph.zion.signal.components.settings.app.chats

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.keyvalue.SignalStore
import seraph.zion.signal.util.BackupUtil
import seraph.zion.signal.util.ConversationUtil
import seraph.zion.signal.util.ThrottledDebouncer
import seraph.zion.signal.util.livedata.Store

class ChatsSettingsViewModel @JvmOverloads constructor(
  private val repository: ChatsSettingsRepository = ChatsSettingsRepository()
) : ViewModel() {

  private val refreshDebouncer = ThrottledDebouncer(500L)

  private val store: Store<ChatsSettingsState> = Store(
    ChatsSettingsState(
      generateLinkPreviews = SignalStore.settings().isLinkPreviewsEnabled,
      useAddressBook = SignalStore.settings().isPreferSystemContactPhotos,
      keepMutedChatsArchived = SignalStore.settings().shouldKeepMutedChatsArchived(),
      useSystemEmoji = SignalStore.settings().isPreferSystemEmoji,
      enterKeySends = SignalStore.settings().isEnterKeySends,
      chatBackupsEnabled = SignalStore.settings().isBackupEnabled && BackupUtil.canUserAccessBackupDirectory(ApplicationDependencies.getApplication())
    )
  )

  val state: LiveData<ChatsSettingsState> = store.stateLiveData

  fun setGenerateLinkPreviewsEnabled(enabled: Boolean) {
    store.update { it.copy(generateLinkPreviews = enabled) }
    SignalStore.settings().isLinkPreviewsEnabled = enabled
    repository.syncLinkPreviewsState()
  }

  fun setUseAddressBook(enabled: Boolean) {
    store.update { it.copy(useAddressBook = enabled) }
    refreshDebouncer.publish { ConversationUtil.refreshRecipientShortcuts() }
    SignalStore.settings().isPreferSystemContactPhotos = enabled
    repository.syncPreferSystemContactPhotos()
  }

  fun setKeepMutedChatsArchived(enabled: Boolean) {
    store.update { it.copy(keepMutedChatsArchived = enabled) }
    SignalStore.settings().setKeepMutedChatsArchived(enabled)
    repository.syncKeepMutedChatsArchivedState()
  }

  fun setUseSystemEmoji(enabled: Boolean) {
    store.update { it.copy(useSystemEmoji = enabled) }
    SignalStore.settings().isPreferSystemEmoji = enabled
  }

  fun setEnterKeySends(enabled: Boolean) {
    store.update { it.copy(enterKeySends = enabled) }
    SignalStore.settings().isEnterKeySends = enabled
  }

  fun refresh() {
    val backupsEnabled = SignalStore.settings().isBackupEnabled && BackupUtil.canUserAccessBackupDirectory(ApplicationDependencies.getApplication())
    if (store.state.chatBackupsEnabled != backupsEnabled) {
      store.update { it.copy(chatBackupsEnabled = backupsEnabled) }
    }
  }
}
