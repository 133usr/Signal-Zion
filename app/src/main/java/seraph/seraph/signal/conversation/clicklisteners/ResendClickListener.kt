/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.conversation.clicklisteners

import android.view.View
import org.signal.core.util.logging.Log
import seraph.zion.signal.database.model.MessageRecord
import seraph.zion.signal.mms.Slide
import seraph.zion.signal.mms.SlidesClickedListener
import seraph.zion.signal.sms.MessageSender

class ResendClickListener(private val messageRecord: MessageRecord) : SlidesClickedListener {
  override fun onClick(v: View?, slides: MutableList<Slide>?) {
    if (v == null) {
      Log.w(TAG, "Could not resend message, view was null!")
      return
    }

    MessageSender.resend(v.context, messageRecord)
  }

  companion object {
    private val TAG = Log.tag(ResendClickListener::class.java)
  }
}
