package seraph.zion.signal.components.settings.conversation.permissions

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import seraph.zion.signal.R
import seraph.zion.signal.components.settings.DSLConfiguration
import seraph.zion.signal.components.settings.DSLSettingsFragment
import seraph.zion.signal.components.settings.DSLSettingsText
import seraph.zion.signal.components.settings.configure
import seraph.zion.signal.groups.ParcelableGroupId
import seraph.zion.signal.groups.ui.GroupErrors
import seraph.zion.signal.util.adapter.mapping.MappingAdapter

class PermissionsSettingsFragment : DSLSettingsFragment(
  titleId = R.string.ConversationSettingsFragment__permissions
) {

  private val permissionsOptions: Array<String> by lazy {
    resources.getStringArray(R.array.PermissionsSettingsFragment__editor_labels)
  }

  private val viewModel: PermissionsSettingsViewModel by viewModels(
    factoryProducer = {
      val args = PermissionsSettingsFragmentArgs.fromBundle(requireArguments())
      val groupId = requireNotNull(ParcelableGroupId.get(args.groupId as ParcelableGroupId))
      val repository = PermissionsSettingsRepository(requireContext())

      PermissionsSettingsViewModel.Factory(groupId, repository)
    }
  )

  override fun bindAdapter(adapter: MappingAdapter) {
    viewModel.state.observe(viewLifecycleOwner) { state ->
      adapter.submitList(getConfiguration(state).toMappingModelList())
    }

    viewModel.events.observe(viewLifecycleOwner) { event ->
      when (event) {
        is PermissionsSettingsEvents.GroupChangeError -> handleGroupChangeError(event)
      }
    }
  }

  private fun handleGroupChangeError(groupChangeError: PermissionsSettingsEvents.GroupChangeError) {
    Toast.makeText(context, GroupErrors.getUserDisplayMessage(groupChangeError.reason), Toast.LENGTH_LONG).show()
  }

  private fun getConfiguration(state: PermissionsSettingsState): DSLConfiguration {
    return configure {
      radioListPref(
        title = DSLSettingsText.from(R.string.PermissionsSettingsFragment__add_members),
        isEnabled = state.selfCanEditSettings,
        listItems = permissionsOptions,
        dialogTitle = DSLSettingsText.from(R.string.PermissionsSettingsFragment__who_can_add_new_members),
        selected = getSelected(state.nonAdminCanAddMembers),
        confirmAction = true,
        onSelected = {
          viewModel.setNonAdminCanAddMembers(it == 1)
        }
      )

      radioListPref(
        title = DSLSettingsText.from(R.string.PermissionsSettingsFragment__edit_group_info),
        isEnabled = state.selfCanEditSettings,
        listItems = permissionsOptions,
        dialogTitle = DSLSettingsText.from(R.string.PermissionsSettingsFragment__who_can_edit_this_groups_info),
        selected = getSelected(state.nonAdminCanEditGroupInfo),
        confirmAction = true,
        onSelected = {
          viewModel.setNonAdminCanEditGroupInfo(it == 1)
        }
      )

      radioListPref(
        title = DSLSettingsText.from(R.string.PermissionsSettingsFragment__send_messages),
        isEnabled = state.selfCanEditSettings,
        listItems = permissionsOptions,
        dialogTitle = DSLSettingsText.from(R.string.PermissionsSettingsFragment__who_can_send_messages),
        selected = getSelected(!state.announcementGroup),
        confirmAction = true,
        onSelected = {
          viewModel.setAnnouncementGroup(it == 0)
        }
      )
    }
  }

  @StringRes
  private fun getSelected(isNonAdminAllowed: Boolean): Int {
    return if (isNonAdminAllowed) {
      1
    } else {
      0
    }
  }
}
