package seraph.zion.signal.mediasend.v2.text

import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import androidx.constraintlayout.widget.Group
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import seraph.zion.signal.R
import seraph.zion.signal.components.KeyboardEntryDialogFragment
import seraph.zion.signal.linkpreview.LinkPreviewRepository
import seraph.zion.signal.linkpreview.LinkPreviewViewModel
import seraph.zion.signal.stories.StoryLinkPreviewView
import seraph.zion.signal.util.LinkUtil
import seraph.zion.signal.util.TextSecurePreferences
import seraph.zion.signal.util.ViewUtil
import seraph.zion.signal.util.setIncognitoKeyboardEnabled
import seraph.zion.signal.util.visible

class TextStoryPostLinkEntryFragment : KeyboardEntryDialogFragment(
  contentLayoutId = R.layout.stories_text_post_link_entry_fragment
) {

  private lateinit var input: EditText

  private val linkPreviewViewModel: LinkPreviewViewModel by viewModels(
    factoryProducer = { LinkPreviewViewModel.Factory(LinkPreviewRepository(), true) }
  )

  private val viewModel: TextStoryPostCreationViewModel by activityViewModels()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    input = view.findViewById(R.id.input)

    val linkPreview: StoryLinkPreviewView = view.findViewById(R.id.link_preview)
    val confirmButton: View = view.findViewById(R.id.confirm_button)
    val shareALinkGroup: Group = view.findViewById(R.id.share_a_link_group)

    input.setIncognitoKeyboardEnabled(TextSecurePreferences.isIncognitoKeyboardEnabled(requireContext()))
    input.addTextChangedListener(
      afterTextChanged = {
        val scheme = "https://"
        val (uriString, selectionStart, selectionEnd) = if (it!!.startsWith(scheme)) {
          Triple(it, input.selectionStart, input.selectionEnd)
        } else {
          Triple("$scheme$it", input.selectionStart + scheme.length, input.selectionEnd + scheme.length)
        }

        linkPreviewViewModel.onTextChanged(requireContext(), uriString.toString(), selectionStart, selectionEnd)
      }
    )

    confirmButton.isEnabled = false
    confirmButton.setOnClickListener {
      val linkPreviewState = linkPreviewViewModel.linkPreviewState.value
      if (linkPreviewState != null) {
        val url = linkPreviewState.linkPreview.map { it.url }.orElseGet { linkPreviewState.activeUrlForError }

        if (LinkUtil.isValidTextStoryPostPreview(url)) {
          viewModel.setLinkPreview(url)
          dismissAllowingStateLoss()
        } else {
          val snackbar = Snackbar.make(requireView(), R.string.TextStoryPostSendFragment__please_enter_a_valid_link, Snackbar.LENGTH_SHORT)
          snackbar.anchorView = linkPreview
          snackbar.show()
        }
      } else {
        dismissAllowingStateLoss()
      }
    }

    linkPreviewViewModel.linkPreviewState.observe(viewLifecycleOwner) { state ->
      linkPreview.bind(state, useLargeThumbnail = false)
      shareALinkGroup.visible = !state.isLoading && !state.linkPreview.isPresent && (state.error == null && state.activeUrlForError == null)
      confirmButton.isEnabled = state.linkPreview.isPresent || !TextUtils.isEmpty(state.activeUrlForError)
    }
  }

  override fun onResume() {
    super.onResume()
    ViewUtil.focusAndShowKeyboard(input)
  }

  override fun onDismiss(dialog: DialogInterface) {
    linkPreviewViewModel.onSend()
    super.onDismiss(dialog)
  }
}
