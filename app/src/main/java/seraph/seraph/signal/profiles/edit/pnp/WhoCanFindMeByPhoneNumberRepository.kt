package seraph.zion.signal.profiles.edit.pnp

import io.reactivex.rxjava3.core.Completable
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.jobs.ProfileUploadJob
import seraph.zion.signal.jobs.RefreshAttributesJob
import seraph.zion.signal.keyvalue.PhoneNumberPrivacyValues
import seraph.zion.signal.keyvalue.SignalStore
import seraph.zion.signal.storage.StorageSyncHelper

/**
 * Manages the current phone-number listing state.
 */
class WhoCanFindMeByPhoneNumberRepository {

  fun getCurrentState(): WhoCanFindMeByPhoneNumberState {
    return when (SignalStore.phoneNumberPrivacy().phoneNumberDiscoverabilityMode) {
      PhoneNumberPrivacyValues.PhoneNumberDiscoverabilityMode.DISCOVERABLE -> WhoCanFindMeByPhoneNumberState.EVERYONE
      PhoneNumberPrivacyValues.PhoneNumberDiscoverabilityMode.NOT_DISCOVERABLE -> WhoCanFindMeByPhoneNumberState.NOBODY
    }
  }

  fun onSave(whoCanFindMeByPhoneNumberState: WhoCanFindMeByPhoneNumberState): Completable {
    return Completable.fromAction {
      when (whoCanFindMeByPhoneNumberState) {
        WhoCanFindMeByPhoneNumberState.EVERYONE -> {
          SignalStore.phoneNumberPrivacy().phoneNumberDiscoverabilityMode = PhoneNumberPrivacyValues.PhoneNumberDiscoverabilityMode.DISCOVERABLE
        }
        WhoCanFindMeByPhoneNumberState.NOBODY -> {
          SignalStore.phoneNumberPrivacy().phoneNumberSharingMode = PhoneNumberPrivacyValues.PhoneNumberSharingMode.NOBODY
          SignalStore.phoneNumberPrivacy().phoneNumberDiscoverabilityMode = PhoneNumberPrivacyValues.PhoneNumberDiscoverabilityMode.NOT_DISCOVERABLE
        }
      }

      ApplicationDependencies.getJobManager().add(RefreshAttributesJob())
      StorageSyncHelper.scheduleSyncForDataChange()
      ApplicationDependencies.getJobManager().add(ProfileUploadJob())
    }
  }
}
