/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.conversation.v2

import seraph.zion.signal.groups.GroupId
import seraph.zion.signal.recipients.Recipient

/**
 * Indicates if we should present an additional review warning banner
 * for an individual or group.
 */
data class RequestReviewState(
  val individualReviewState: IndividualReviewState? = null,
  val groupReviewState: GroupReviewState? = null
) {

  fun shouldShowReviewBanner(): Boolean {
    return individualReviewState != null || groupReviewState != null
  }

  /** Recipient is in message request state and has similar name as someone else */
  data class IndividualReviewState(val recipient: Recipient)

  /** Group has multiple members with similar names */
  data class GroupReviewState(val groupId: GroupId.V2, val recipient: Recipient, val count: Int)
}
