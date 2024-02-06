/**
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.calls.links.create

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import seraph.zion.signal.database.CallLinkTable
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.recipients.Recipient
import seraph.zion.signal.recipients.RecipientId
import seraph.zion.signal.service.webrtc.links.CallLinkCredentials
import seraph.zion.signal.service.webrtc.links.CreateCallLinkResult
import seraph.zion.signal.service.webrtc.links.SignalCallLinkManager

/**
 * Repository for creating new call links. This will delegate to the [SignalCallLinkManager]
 * but will also ensure the database is updated.
 */
class CreateCallLinkRepository(
  private val callLinkManager: SignalCallLinkManager = ApplicationDependencies.getSignalCallManager().callLinkManager
) {
  fun ensureCallLinkCreated(credentials: CallLinkCredentials): Single<EnsureCallLinkCreatedResult> {
    val callLinkRecipientId = Single.fromCallable {
      SignalDatabase.recipients.getByCallLinkRoomId(credentials.roomId)
    }

    return callLinkRecipientId.flatMap { recipientId ->
      if (recipientId.isPresent) {
        Single.just(EnsureCallLinkCreatedResult.Success(Recipient.resolved(recipientId.get())))
      } else {
        callLinkManager.createCallLink(credentials).map {
          when (it) {
            is CreateCallLinkResult.Success -> {
              SignalDatabase.callLinks.insertCallLink(
                CallLinkTable.CallLink(
                  recipientId = RecipientId.UNKNOWN,
                  roomId = credentials.roomId,
                  credentials = credentials,
                  state = it.state
                )
              )

              EnsureCallLinkCreatedResult.Success(
                Recipient.resolved(
                  SignalDatabase.recipients.getByCallLinkRoomId(credentials.roomId).get()
                )
              )
            }

            is CreateCallLinkResult.Failure -> EnsureCallLinkCreatedResult.Failure(it)
          }
        }
      }
    }.subscribeOn(Schedulers.io())
  }
}
