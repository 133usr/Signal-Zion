package seraph.zion.signal.components.settings.app.changenumber

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import seraph.zion.signal.LoggingFragment
import seraph.zion.signal.R
import seraph.zion.signal.components.settings.app.changenumber.ChangeNumberUtil.changeNumberSuccess
import seraph.zion.signal.lock.v2.CreateSvrPinActivity

class ChangeNumberPinDiffersFragment : LoggingFragment(R.layout.fragment_change_number_pin_differs) {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    view.findViewById<View>(R.id.change_number_pin_differs_keep_old_pin).setOnClickListener {
      changeNumberSuccess()
    }

    val changePin = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == CreateSvrPinActivity.RESULT_OK) {
        changeNumberSuccess()
      }
    }

    view.findViewById<View>(R.id.change_number_pin_differs_update_pin).setOnClickListener {
      changePin.launch(CreateSvrPinActivity.getIntentForPinChangeFromSettings(requireContext()))
    }

    requireActivity().onBackPressedDispatcher.addCallback(
      viewLifecycleOwner,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.ChangeNumberPinDiffersFragment__keep_old_pin_question)
            .setPositiveButton(android.R.string.ok) { _, _ -> changeNumberSuccess() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
        }
      }
    )
  }
}
