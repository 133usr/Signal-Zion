package seraph.zion.signal.registration.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.ActivityNavigator
import org.signal.core.util.logging.Log
import seraph.zion.signal.LoggingFragment
import seraph.zion.signal.MainActivity
import seraph.zion.signal.R
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.jobs.MultiDeviceProfileContentUpdateJob
import seraph.zion.signal.jobs.MultiDeviceProfileKeyUpdateJob
import seraph.zion.signal.jobs.ProfileUploadJob
import seraph.zion.signal.keyvalue.SignalStore
import seraph.zion.signal.lock.v2.CreateSvrPinActivity
import seraph.zion.signal.pin.PinRestoreActivity
import seraph.zion.signal.profiles.AvatarHelper
import seraph.zion.signal.profiles.edit.CreateProfileActivity
import seraph.zion.signal.recipients.Recipient
import seraph.zion.signal.registration.RegistrationUtil
import seraph.zion.signal.registration.viewmodel.RegistrationViewModel

/**
 * [RegistrationCompleteFragment] is not visible to the user, but functions as basically a redirect towards one of:
 * - [PIN Restore flow activity](seraph.zion.signal.pin.PinRestoreActivity)
 * - [Profile](seraph.zion.signal.profiles.edit.EditProfileActivity) / [PIN creation](seraph.zion.signal.lock.v2.CreateKbsPinActivity) flow activities (this class chains the necessary activities together as an intent)
 * - Exit registration flow and progress to conversation list
 */
class RegistrationCompleteFragment : LoggingFragment() {
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_registration_blank, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val activity = requireActivity()
    val viewModel: RegistrationViewModel by viewModels(ownerProducer = { requireActivity() })

    if (SignalStore.misc().hasLinkedDevices) {
      SignalStore.misc().shouldShowLinkedDevicesReminder = viewModel.isReregister
    }

    if (SignalStore.storageService().needsAccountRestore()) {
      Log.i(TAG, "Performing pin restore.")
      activity.startActivity(Intent(activity, PinRestoreActivity::class.java))
    } else {
      val isProfileNameEmpty = Recipient.self().profileName.isEmpty
      val isAvatarEmpty = !AvatarHelper.hasAvatar(activity, Recipient.self().id)
      val needsProfile = isProfileNameEmpty || isAvatarEmpty
      val needsPin = !SignalStore.svr().hasPin() && !viewModel.isReregister

      Log.i(TAG, "Pin restore flow not required. Profile name: $isProfileNameEmpty | Profile avatar: $isAvatarEmpty | Needs PIN: $needsPin")

      if (!needsProfile && !needsPin) {
        ApplicationDependencies.getJobManager()
          .startChain(ProfileUploadJob())
          .then(listOf(MultiDeviceProfileKeyUpdateJob(), MultiDeviceProfileContentUpdateJob()))
          .enqueue()
        RegistrationUtil.maybeMarkRegistrationComplete()
      }

      var startIntent = MainActivity.clearTop(activity)

      if (needsPin) {
        startIntent = chainIntents(CreateSvrPinActivity.getIntentForPinCreate(activity), startIntent)
      }

      if (needsProfile) {
        startIntent = chainIntents(CreateProfileActivity.getIntentForUserProfile(activity), startIntent)
      }

      activity.startActivity(startIntent)
    }

    activity.finish()
    ActivityNavigator.applyPopAnimationsToPendingTransition(activity)
  }

  private fun chainIntents(sourceIntent: Intent, nextIntent: Intent): Intent {
    sourceIntent.putExtra("next_intent", nextIntent)
    return sourceIntent
  }

  companion object {
    private val TAG = Log.tag(RegistrationCompleteFragment::class.java)
  }
}
