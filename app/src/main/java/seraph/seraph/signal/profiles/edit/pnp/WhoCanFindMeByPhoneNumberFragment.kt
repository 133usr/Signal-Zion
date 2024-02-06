package seraph.zion.signal.profiles.edit.pnp

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.signal.core.util.concurrent.LifecycleDisposable
import seraph.zion.signal.R
import seraph.zion.signal.components.ViewBinderDelegate
import seraph.zion.signal.components.settings.DSLConfiguration
import seraph.zion.signal.components.settings.DSLSettingsFragment
import seraph.zion.signal.components.settings.DSLSettingsText
import seraph.zion.signal.components.settings.configure
import seraph.zion.signal.databinding.WhoCanFindMeByPhoneNumberFragmentBinding
import seraph.zion.signal.util.FeatureFlags
import seraph.zion.signal.util.adapter.mapping.MappingAdapter

/**
 * Allows the user to select who can see their phone number during registration.
 */
class WhoCanFindMeByPhoneNumberFragment : DSLSettingsFragment(
  titleId = R.string.WhoCanSeeMyPhoneNumberFragment__who_can_find_me_by_number,
  layoutId = R.layout.who_can_find_me_by_phone_number_fragment
) {

  companion object {
    /**
     * Components can listen to this result to know when the user hit the submit button.
     */
    const val REQUEST_KEY = "who_can_see_my_phone_number_key"
  }

  private val viewModel: WhoCanFindMeByPhoneNumberViewModel by viewModels()
  private val lifecycleDisposable = LifecycleDisposable()

  private val binding by ViewBinderDelegate(WhoCanFindMeByPhoneNumberFragmentBinding::bind)

  override fun bindAdapter(adapter: MappingAdapter) {
    require(FeatureFlags.phoneNumberPrivacy())

    lifecycleDisposable += viewModel.state.subscribe {
      adapter.submitList(getConfiguration(it).toMappingModelList())
    }

    binding.save.setOnClickListener {
      binding.save.isEnabled = false
      lifecycleDisposable += viewModel.onSave().subscribeBy(onComplete = {
        setFragmentResult(REQUEST_KEY, Bundle())
        findNavController().popBackStack()
      })
    }
  }

  private fun getConfiguration(state: WhoCanFindMeByPhoneNumberState): DSLConfiguration {
    return configure {
      radioPref(
        title = DSLSettingsText.from(R.string.PhoneNumberPrivacy_everyone),
        isChecked = state == WhoCanFindMeByPhoneNumberState.EVERYONE,
        onClick = { viewModel.onEveryoneCanFindMeByPhoneNumberSelected() }
      )

      radioPref(
        title = DSLSettingsText.from(R.string.PhoneNumberPrivacy_nobody),
        isChecked = state == WhoCanFindMeByPhoneNumberState.NOBODY,
        onClick = { viewModel.onNobodyCanFindMeByPhoneNumberSelected() }
      )

      textPref(
        title = DSLSettingsText.from(
          when (state) {
            WhoCanFindMeByPhoneNumberState.EVERYONE -> R.string.WhoCanSeeMyPhoneNumberFragment__anyone_who_has_your
            WhoCanFindMeByPhoneNumberState.NOBODY -> R.string.WhoCanSeeMyPhoneNumberFragment__nobody_will_be_able
          },
          DSLSettingsText.TextAppearanceModifier(R.style.Signal_Text_BodyMedium),
          DSLSettingsText.ColorModifier(ContextCompat.getColor(requireContext(), R.color.signal_colorOnSurfaceVariant))
        )
      )
    }
  }
}
