/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.components.settings.app.subscription.completed

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import seraph.zion.signal.badges.Badges
import seraph.zion.signal.badges.models.Badge
import seraph.zion.signal.database.model.databaseprotos.TerminalDonationQueue
import seraph.zion.signal.dependencies.ApplicationDependencies
import org.whispersystems.signalservice.api.services.DonationsService
import java.util.Locale

class TerminalDonationRepository(
  private val donationsService: DonationsService = ApplicationDependencies.getDonationsService()
) {
  fun getBadge(terminalDonation: TerminalDonationQueue.TerminalDonation): Single<Badge> {
    return Single
      .fromCallable { donationsService.getDonationsConfiguration(Locale.getDefault()) }
      .flatMap { it.flattenResult() }
      .map { it.levels[terminalDonation.level.toInt()]!! }
      .map { Badges.fromServiceBadge(it.badge) }
      .subscribeOn(Schedulers.io())
  }
}
