package seraph.zion.signal.components.settings.app.subscription.receipts.list

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import seraph.zion.signal.R
import seraph.zion.signal.badges.models.Badge
import seraph.zion.signal.components.settings.DSLSettingsText
import seraph.zion.signal.components.settings.TextPreference
import seraph.zion.signal.database.model.DonationReceiptRecord
import seraph.zion.signal.util.StickyHeaderDecoration
import seraph.zion.signal.util.livedata.LiveDataUtil
import seraph.zion.signal.util.navigation.safeNavigate
import seraph.zion.signal.util.visible

class DonationReceiptListPageFragment : Fragment(R.layout.donation_receipt_list_page_fragment) {

  private val viewModel: DonationReceiptListPageViewModel by viewModels(factoryProducer = {
    DonationReceiptListPageViewModel.Factory(type, DonationReceiptListPageRepository())
  })

  private val sharedViewModel: DonationReceiptListViewModel by viewModels(
    ownerProducer = { requireParentFragment() },
    factoryProducer = {
      DonationReceiptListViewModel.Factory(DonationReceiptListRepository())
    }
  )

  private val type: DonationReceiptRecord.Type?
    get() = requireArguments().getString(ARG_TYPE)?.let { DonationReceiptRecord.Type.fromCode(it) }

  private lateinit var emptyStateGroup: Group

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val adapter = DonationReceiptListAdapter { model ->
      findNavController().safeNavigate(DonationReceiptListFragmentDirections.actionDonationReceiptListFragmentToDonationReceiptDetailFragment(model.record.id))
    }

    view.findViewById<RecyclerView>(R.id.recycler).apply {
      this.adapter = adapter
      addItemDecoration(StickyHeaderDecoration(adapter, false, true, 0))
    }

    emptyStateGroup = view.findViewById(R.id.empty_state)

    LiveDataUtil.combineLatest(
      viewModel.state,
      sharedViewModel.state
    ) { state, badges ->
      state.isLoaded to state.records.map { DonationReceiptListItem.Model(it, getBadgeForRecord(it, badges)) }
    }.observe(viewLifecycleOwner) { (isLoaded, records) ->
      if (records.isNotEmpty()) {
        emptyStateGroup.visible = false
        adapter.submitList(
          records +
            TextPreference(
              title = null,
              summary = DSLSettingsText.from(
                R.string.DonationReceiptListFragment__if_you_have,
                DSLSettingsText.TextAppearanceModifier(R.style.TextAppearance_Signal_Subtitle)
              )
            )
        )
      } else {
        emptyStateGroup.visible = isLoaded
      }
    }
  }

  private fun getBadgeForRecord(record: DonationReceiptRecord, badges: List<DonationReceiptBadge>): Badge? {
    return when (record.type) {
      DonationReceiptRecord.Type.BOOST -> badges.firstOrNull { it.type == DonationReceiptRecord.Type.BOOST }?.badge
      DonationReceiptRecord.Type.GIFT -> badges.firstOrNull { it.type == DonationReceiptRecord.Type.GIFT }?.badge
      else -> badges.firstOrNull { it.level == record.subscriptionLevel }?.badge
    }
  }

  companion object {

    private const val ARG_TYPE = "arg_type"

    fun create(type: DonationReceiptRecord.Type?): Fragment {
      return DonationReceiptListPageFragment().apply {
        arguments = Bundle().apply {
          putString(ARG_TYPE, type?.code)
        }
      }
    }
  }
}
