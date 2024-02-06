package seraph.zion.signal.conversation.quotes

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.signal.core.util.concurrent.LifecycleDisposable
import seraph.zion.signal.R
import seraph.zion.signal.components.FixedRoundedCornerBottomSheetDialogFragment
import seraph.zion.signal.components.recyclerview.SmoothScrollingLinearLayoutManager
import seraph.zion.signal.conversation.ConversationAdapter
import seraph.zion.signal.conversation.ConversationAdapterBridge
import seraph.zion.signal.conversation.ConversationBottomSheetCallback
import seraph.zion.signal.conversation.ConversationItemDisplayMode
import seraph.zion.signal.conversation.colors.Colorizer
import seraph.zion.signal.conversation.colors.RecyclerViewColorizer
import seraph.zion.signal.conversation.mutiselect.MultiselectPart
import seraph.zion.signal.database.model.MessageId
import seraph.zion.signal.database.model.MessageRecord
import seraph.zion.signal.database.model.MmsMessageRecord
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.giph.mp4.GiphyMp4ItemDecoration
import seraph.zion.signal.giph.mp4.GiphyMp4PlaybackController
import seraph.zion.signal.giph.mp4.GiphyMp4PlaybackPolicy
import seraph.zion.signal.giph.mp4.GiphyMp4ProjectionPlayerHolder
import seraph.zion.signal.giph.mp4.GiphyMp4ProjectionRecycler
import seraph.zion.signal.groups.GroupId
import seraph.zion.signal.groups.GroupMigrationMembershipChange
import seraph.zion.signal.linkpreview.LinkPreview
import seraph.zion.signal.recipients.Recipient
import seraph.zion.signal.recipients.RecipientId
import seraph.zion.signal.util.BottomSheetUtil
import seraph.zion.signal.util.StickyHeaderDecoration
import seraph.zion.signal.util.fragments.findListener
import java.util.Locale

class MessageQuotesBottomSheet : FixedRoundedCornerBottomSheetDialogFragment() {

  override val peekHeightPercentage: Float = 0.66f
  override val themeResId: Int = R.style.Widget_Signal_FixedRoundedCorners_Messages

  private lateinit var messageAdapter: ConversationAdapter
  private val viewModel: MessageQuotesViewModel by viewModels(
    factoryProducer = {
      val messageId = MessageId.deserialize(arguments?.getString(KEY_MESSAGE_ID, null) ?: throw IllegalArgumentException())
      val conversationRecipientId = RecipientId.from(arguments?.getString(KEY_CONVERSATION_RECIPIENT_ID, null) ?: throw IllegalArgumentException())
      MessageQuotesViewModel.Factory(ApplicationDependencies.getApplication(), messageId, conversationRecipientId)
    }
  )

  private val disposables: LifecycleDisposable = LifecycleDisposable()
  private var firstRender: Boolean = true

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val view = inflater.inflate(R.layout.message_quotes_bottom_sheet, container, false)
    disposables.bindTo(viewLifecycleOwner)
    return view
  }

  @SuppressLint("WrongThread")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val conversationRecipientId = RecipientId.from(arguments?.getString(KEY_CONVERSATION_RECIPIENT_ID, null) ?: throw IllegalArgumentException())
    val conversationRecipient = Recipient.resolved(conversationRecipientId)

    val colorizer = Colorizer()

    messageAdapter = ConversationAdapter(requireContext(), viewLifecycleOwner, Glide.with(this), Locale.getDefault(), ConversationAdapterListener(), conversationRecipient.hasWallpaper(), colorizer).apply {
      setCondensedMode(ConversationItemDisplayMode.Condensed(scheduleMessageMode = false))
    }

    val list: RecyclerView = view.findViewById<RecyclerView>(R.id.quotes_list).apply {
      layoutManager = SmoothScrollingLinearLayoutManager(requireContext(), true)
      adapter = messageAdapter
      itemAnimator = null
      addItemDecoration(OriginalMessageSeparatorDecoration(context, R.string.MessageQuotesBottomSheet_replies))

      doOnNextLayout {
        // Adding this without waiting for a layout pass would result in an indeterminate amount of padding added to the top of the view
        addItemDecoration(StickyHeaderDecoration(messageAdapter, false, false, ConversationAdapter.HEADER_TYPE_INLINE_DATE))
      }
    }

    val recyclerViewColorizer = RecyclerViewColorizer(list)

    disposables += viewModel.getMessages().subscribe { messages ->
      if (messages.isEmpty()) {
        dismiss()
      }

      messageAdapter.submitList(messages) {
        if (firstRender) {
          val targetMessageId = MessageId.deserialize(arguments?.getString(KEY_MESSAGE_ID, null) ?: throw IllegalArgumentException())
          val targetMessagePosition = messages.indexOfFirst { it.messageRecord.id == targetMessageId.id }

          (list.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(targetMessagePosition, 100)

          if (targetMessagePosition != messages.size - 1) {
            (dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED
            messageAdapter.pulseAtPosition(targetMessagePosition)
          }

          firstRender = false
        } else if (!list.canScrollVertically(1)) {
          list.layoutManager?.scrollToPosition(0)
        }
      }
      recyclerViewColorizer.setChatColors(conversationRecipient.chatColors)
    }

    disposables += viewModel.getNameColorsMap().subscribe { map ->
      colorizer.onNameColorsChanged(map)
      messageAdapter.notifyItemRangeChanged(0, messageAdapter.itemCount, ConversationAdapterBridge.PAYLOAD_NAME_COLORS)
    }

    initializeGiphyMp4(view.findViewById(R.id.video_container) as ViewGroup, list)
  }

  private fun initializeGiphyMp4(videoContainer: ViewGroup, list: RecyclerView): GiphyMp4ProjectionRecycler {
    val maxPlayback = GiphyMp4PlaybackPolicy.maxSimultaneousPlaybackInConversation()
    val holders = GiphyMp4ProjectionPlayerHolder.injectVideoViews(
      requireContext(),
      viewLifecycleOwner.lifecycle,
      videoContainer,
      maxPlayback
    )
    val callback = GiphyMp4ProjectionRecycler(holders)

    GiphyMp4PlaybackController.attach(list, callback, maxPlayback)
    list.addItemDecoration(GiphyMp4ItemDecoration(callback) {}, 0)

    return callback
  }

  private fun getCallback(): ConversationBottomSheetCallback {
    return findListener<ConversationBottomSheetCallback>() ?: throw IllegalStateException("Parent must implement callback interface!")
  }

  private fun getAdapterListener(): ConversationAdapter.ItemClickListener {
    return getCallback().getConversationAdapterListener()
  }

  private inner class ConversationAdapterListener : ConversationAdapter.ItemClickListener by getAdapterListener() {
    override fun onItemClick(item: MultiselectPart) {
      dismiss()
      getCallback().jumpToMessage(item.getMessageRecord())
    }

    override fun onItemLongClick(itemView: View, item: MultiselectPart) {
      onItemClick(item)
    }

    override fun onQuoteClicked(messageRecord: MmsMessageRecord) {
      dismiss()
      getCallback().jumpToMessage(messageRecord)
    }

    override fun onLinkPreviewClicked(linkPreview: LinkPreview) {
      dismiss()
      getAdapterListener().onLinkPreviewClicked(linkPreview)
    }

    override fun onQuotedIndicatorClicked(messageRecord: MessageRecord) {
      dismiss()
      getAdapterListener().onQuotedIndicatorClicked(messageRecord)
    }

    override fun onReactionClicked(multiselectPart: MultiselectPart, messageId: Long, isMms: Boolean) {
      dismiss()
      getCallback().jumpToMessage(multiselectPart.conversationMessage.messageRecord)
    }

    override fun onGroupMemberClicked(recipientId: RecipientId, groupId: GroupId) {
      dismiss()
      getAdapterListener().onGroupMemberClicked(recipientId, groupId)
    }

    override fun onMessageWithRecaptchaNeededClicked(messageRecord: MessageRecord) {
      dismiss()
      getAdapterListener().onMessageWithRecaptchaNeededClicked(messageRecord)
    }

    override fun onGroupMigrationLearnMoreClicked(membershipChange: GroupMigrationMembershipChange) {
      dismiss()
      getAdapterListener().onGroupMigrationLearnMoreClicked(membershipChange)
    }

    override fun onChatSessionRefreshLearnMoreClicked() {
      dismiss()
      getAdapterListener().onChatSessionRefreshLearnMoreClicked()
    }

    override fun onBadDecryptLearnMoreClicked(author: RecipientId) {
      dismiss()
      getAdapterListener().onBadDecryptLearnMoreClicked(author)
    }

    override fun onSafetyNumberLearnMoreClicked(recipient: Recipient) {
      dismiss()
      getAdapterListener().onSafetyNumberLearnMoreClicked(recipient)
    }

    override fun onJoinGroupCallClicked() {
      dismiss()
      getAdapterListener().onJoinGroupCallClicked()
    }

    override fun onInviteFriendsToGroupClicked(groupId: GroupId.V2) {
      dismiss()
      getAdapterListener().onInviteFriendsToGroupClicked(groupId)
    }

    override fun onEnableCallNotificationsClicked() {
      dismiss()
      getAdapterListener().onEnableCallNotificationsClicked()
    }

    override fun onCallToAction(action: String) {
      dismiss()
      getAdapterListener().onCallToAction(action)
    }

    override fun onDonateClicked() {
      dismiss()
      getAdapterListener().onDonateClicked()
    }

    override fun onRecipientNameClicked(target: RecipientId) {
      dismiss()
      getAdapterListener().onRecipientNameClicked(target)
    }

    override fun onViewGiftBadgeClicked(messageRecord: MessageRecord) {
      dismiss()
      getAdapterListener().onViewGiftBadgeClicked(messageRecord)
    }

    override fun onActivatePaymentsClicked() {
      dismiss()
      getAdapterListener().onActivatePaymentsClicked()
    }

    override fun onSendPaymentClicked(recipientId: RecipientId) {
      dismiss()
      getAdapterListener().onSendPaymentClicked(recipientId)
    }

    override fun onEditedIndicatorClicked(messageRecord: MessageRecord) {
      dismiss()
      getAdapterListener().onEditedIndicatorClicked(messageRecord)
    }
  }

  companion object {
    private const val KEY_MESSAGE_ID = "message_id"
    private const val KEY_CONVERSATION_RECIPIENT_ID = "conversation_recipient_id"

    @JvmStatic
    fun show(fragmentManager: FragmentManager, messageId: MessageId, conversationRecipientId: RecipientId) {
      val args = Bundle().apply {
        putString(KEY_MESSAGE_ID, messageId.serialize())
        putString(KEY_CONVERSATION_RECIPIENT_ID, conversationRecipientId.serialize())
      }

      val fragment = MessageQuotesBottomSheet().apply {
        arguments = args
      }

      fragment.show(fragmentManager, BottomSheetUtil.STANDARD_BOTTOM_SHEET_FRAGMENT_TAG)
    }
  }
}
