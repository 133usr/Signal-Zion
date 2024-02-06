/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.recipients.ui.about

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.recipients.RecipientId

class AboutSheetRepository {
  fun getGroupsInCommonCount(recipientId: RecipientId): Single<Int> {
    return Single.fromCallable {
      SignalDatabase.groups.getPushGroupsContainingMember(recipientId).size
    }.subscribeOn(Schedulers.io())
  }
}
