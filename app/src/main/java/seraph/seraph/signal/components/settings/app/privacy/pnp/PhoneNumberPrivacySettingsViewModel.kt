package seraph.zion.signal.components.settings.app.privacy.pnp

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.jobs.ProfileUploadJob
import seraph.zion.signal.jobs.RefreshAttributesJob
import seraph.zion.signal.jobs.RefreshOwnProfileJob
import seraph.zion.signal.keyvalue.PhoneNumberPrivacyValues.PhoneNumberDiscoverabilityMode
import seraph.zion.signal.keyvalue.PhoneNumberPrivacyValues.PhoneNumberSharingMode
import seraph.zion.signal.keyvalue.SignalStore
import seraph.zion.signal.recipients.Recipient
import seraph.zion.signal.storage.StorageSyncHelper

class PhoneNumberPrivacySettingsViewModel : ViewModel() {

  private val _state = mutableStateOf(
    PhoneNumberPrivacySettingsState(
      phoneNumberSharing = SignalStore.phoneNumberPrivacy().isPhoneNumberSharingEnabled,
      discoverableByPhoneNumber = SignalStore.phoneNumberPrivacy().isDiscoverableByPhoneNumber
    )
  )

  val state: State<PhoneNumberPrivacySettingsState> = _state

  fun setNobodyCanSeeMyNumber() {
    setPhoneNumberSharingEnabled(false)
  }

  fun setEveryoneCanSeeMyNumber() {
    setPhoneNumberSharingEnabled(true)
    setDiscoverableByPhoneNumber(true)
  }

  fun setNobodyCanFindMeByMyNumber() {
    setDiscoverableByPhoneNumber(false)
  }

  fun setEveryoneCanFindMeByMyNumber() {
    setDiscoverableByPhoneNumber(true)
  }

  private fun setPhoneNumberSharingEnabled(phoneNumberSharingEnabled: Boolean) {
    SignalStore.phoneNumberPrivacy().phoneNumberSharingMode = if (phoneNumberSharingEnabled) PhoneNumberSharingMode.EVERYBODY else PhoneNumberSharingMode.NOBODY
    SignalDatabase.recipients.markNeedsSync(Recipient.self().id)
    StorageSyncHelper.scheduleSyncForDataChange()
    ApplicationDependencies.getJobManager().add(ProfileUploadJob())
    refresh()
  }

  private fun setDiscoverableByPhoneNumber(discoverable: Boolean) {
    SignalStore.phoneNumberPrivacy().phoneNumberDiscoverabilityMode = if (discoverable) PhoneNumberDiscoverabilityMode.DISCOVERABLE else PhoneNumberDiscoverabilityMode.NOT_DISCOVERABLE
    StorageSyncHelper.scheduleSyncForDataChange()
    ApplicationDependencies.getJobManager().startChain(RefreshAttributesJob()).then(RefreshOwnProfileJob()).enqueue()
    refresh()
  }

  fun refresh() {
    _state.value = PhoneNumberPrivacySettingsState(
      phoneNumberSharing = SignalStore.phoneNumberPrivacy().isPhoneNumberSharingEnabled,
      discoverableByPhoneNumber = SignalStore.phoneNumberPrivacy().isDiscoverableByPhoneNumber
    )
  }
}
