package seraph.zion.signal.components.settings.app.changenumber

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import seraph.zion.signal.R
import seraph.zion.signal.components.settings.app.changenumber.ChangeNumberUtil.changeNumberSuccess
import seraph.zion.signal.keyvalue.SignalStore
import seraph.zion.signal.registration.fragments.BaseRegistrationLockFragment
import seraph.zion.signal.registration.viewmodel.BaseRegistrationViewModel
import seraph.zion.signal.util.CommunicationActions
import seraph.zion.signal.util.SupportEmailUtil
import seraph.zion.signal.util.navigation.safeNavigate
import org.whispersystems.signalservice.api.kbs.PinHashUtil

class ChangeNumberRegistrationLockFragment : BaseRegistrationLockFragment(R.layout.fragment_change_number_registration_lock) {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    toolbar.setNavigationOnClickListener { navigateUp() }

    requireActivity().onBackPressedDispatcher.addCallback(
      viewLifecycleOwner,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          navigateUp()
        }
      }
    )
  }

  override fun getViewModel(): BaseRegistrationViewModel {
    return ChangeNumberUtil.getViewModel(this)
  }

  override fun navigateToAccountLocked() {
    findNavController().safeNavigate(ChangeNumberRegistrationLockFragmentDirections.actionChangeNumberRegistrationLockToChangeNumberAccountLocked())
  }

  override fun handleSuccessfulPinEntry(pin: String) {
    val pinsDiffer: Boolean = SignalStore.svr().localPinHash?.let { !PinHashUtil.verifyLocalPinHash(it, pin) } ?: false

    pinButton.cancelSpinning()

    if (pinsDiffer) {
      findNavController().safeNavigate(ChangeNumberRegistrationLockFragmentDirections.actionChangeNumberRegistrationLockToChangeNumberPinDiffers())
    } else {
      changeNumberSuccess()
    }
  }

  override fun sendEmailToSupport() {
    val subject = R.string.ChangeNumberRegistrationLockFragment__signal_change_number_need_help_with_pin_for_android_v2_pin

    val body: String = SupportEmailUtil.generateSupportEmailBody(
      requireContext(),
      subject,
      null,
      null
    )

    CommunicationActions.openEmail(
      requireContext(),
      SupportEmailUtil.getSupportEmailAddress(requireContext()),
      getString(subject),
      body
    )
  }

  private fun navigateUp() {
    if (SignalStore.misc().isChangeNumberLocked) {
      startActivity(ChangeNumberLockActivity.createIntent(requireContext()))
    } else {
      findNavController().navigateUp()
    }
  }
}
