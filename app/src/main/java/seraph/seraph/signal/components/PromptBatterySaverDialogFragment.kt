/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import org.signal.core.util.concurrent.LifecycleDisposable
import org.signal.core.util.logging.Log
import seraph.zion.signal.R
import seraph.zion.signal.databinding.PromptBatterySaverBottomSheetBinding
import seraph.zion.signal.keyvalue.SignalStore
import seraph.zion.signal.util.BottomSheetUtil
import seraph.zion.signal.util.LocalMetrics
import seraph.zion.signal.util.PowerManagerCompat

@RequiresApi(23)
class PromptBatterySaverDialogFragment : FixedRoundedCornerBottomSheetDialogFragment() {

  companion object {
    private val TAG = Log.tag(PromptBatterySaverDialogFragment::class.java)

    @JvmStatic
    fun show(fragmentManager: FragmentManager) {
      if (fragmentManager.findFragmentByTag(BottomSheetUtil.STANDARD_BOTTOM_SHEET_FRAGMENT_TAG) == null) {
        PromptBatterySaverDialogFragment().apply {
          arguments = bundleOf()
        }.show(fragmentManager, BottomSheetUtil.STANDARD_BOTTOM_SHEET_FRAGMENT_TAG)
        SignalStore.uiHints().lastBatterySaverPrompt = System.currentTimeMillis()
      }
    }
  }

  override val peekHeightPercentage: Float = 0.66f
  override val themeResId: Int = R.style.Widget_Signal_FixedRoundedCorners_Messages

  private val binding by ViewBinderDelegate(PromptBatterySaverBottomSheetBinding::bind)

  private val disposables: LifecycleDisposable = LifecycleDisposable()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.prompt_battery_saver_bottom_sheet, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    disposables.bindTo(viewLifecycleOwner)

    binding.continueButton.setOnClickListener {
      PowerManagerCompat.requestIgnoreBatteryOptimizations(requireContext())
      Log.i(TAG, "Requested to ignore battery optimizations, clearing local metrics.")
      LocalMetrics.clear()
    }
    binding.dismissButton.setOnClickListener {
      Log.i(TAG, "User denied request to ignore battery optimizations.")
      SignalStore.uiHints().markDismissedBatterySaverPrompt()
      dismiss()
    }
  }
}
