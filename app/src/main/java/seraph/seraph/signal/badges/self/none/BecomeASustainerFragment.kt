package seraph.zion.signal.badges.self.none

import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import org.signal.core.util.DimensionUnit
import seraph.zion.signal.R
import seraph.zion.signal.badges.models.BadgePreview
import seraph.zion.signal.components.settings.DSLConfiguration
import seraph.zion.signal.components.settings.DSLSettingsAdapter
import seraph.zion.signal.components.settings.DSLSettingsBottomSheetFragment
import seraph.zion.signal.components.settings.DSLSettingsText
import seraph.zion.signal.components.settings.app.AppSettingsActivity
import seraph.zion.signal.components.settings.app.subscription.MonthlyDonationRepository
import seraph.zion.signal.components.settings.configure
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.util.BottomSheetUtil

class BecomeASustainerFragment : DSLSettingsBottomSheetFragment() {

  private val viewModel: BecomeASustainerViewModel by viewModels(
    factoryProducer = {
      BecomeASustainerViewModel.Factory(MonthlyDonationRepository(ApplicationDependencies.getDonationsService()))
    }
  )

  override fun bindAdapter(adapter: DSLSettingsAdapter) {
    BadgePreview.register(adapter)

    viewModel.state.observe(viewLifecycleOwner) {
      adapter.submitList(getConfiguration(it).toMappingModelList())
    }
  }

  private fun getConfiguration(state: BecomeASustainerState): DSLConfiguration {
    return configure {
      customPref(BadgePreview.BadgeModel.FeaturedModel(badge = state.badge))

      sectionHeaderPref(
        title = DSLSettingsText.from(
          R.string.BecomeASustainerFragment__get_badges,
          DSLSettingsText.CenterModifier,
          DSLSettingsText.TitleLargeModifier
        )
      )

      space(DimensionUnit.DP.toPixels(8f).toInt())

      noPadTextPref(
        title = DSLSettingsText.from(
          R.string.BecomeASustainerFragment__signal_is_a_non_profit,
          DSLSettingsText.CenterModifier,
          DSLSettingsText.TextAppearanceModifier(R.style.Signal_Text_BodyMedium),
          DSLSettingsText.ColorModifier(ContextCompat.getColor(requireContext(), R.color.signal_colorOnSurfaceVariant))
        )
      )

      space(DimensionUnit.DP.toPixels(32f).toInt())

      tonalWrappedButton(
        text = DSLSettingsText.from(
          R.string.BecomeASustainerMegaphone__become_a_sustainer
        ),
        onClick = {
          requireActivity().finish()
          requireActivity().startActivity(AppSettingsActivity.subscriptions(requireContext()).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }
      )

      space(DimensionUnit.DP.toPixels(32f).toInt())
    }
  }

  companion object {
    @JvmStatic
    fun show(fragmentManager: FragmentManager) {
      BecomeASustainerFragment().show(fragmentManager, BottomSheetUtil.STANDARD_BOTTOM_SHEET_FRAGMENT_TAG)
    }
  }
}
