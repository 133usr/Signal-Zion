/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.thoughtcrime.securesms.components.settings.app.subscription.donate

import org.signal.donations.InAppPaymentType
import org.thoughtcrime.securesms.database.InAppPaymentTable
import org.thoughtcrime.securesms.database.model.databaseprotos.BadgeList
import org.thoughtcrime.securesms.database.model.databaseprotos.DecimalValue
import org.thoughtcrime.securesms.database.model.databaseprotos.FiatValue
import org.thoughtcrime.securesms.database.model.databaseprotos.InAppPaymentData
import org.whispersystems.signalservice.api.payments.Money
import org.whispersystems.signalservice.api.subscriptions.SubscriberId
import kotlin.time.Duration

// Mock InAppPaymentTable.InAppPayment
val payId = InAppPaymentTable.InAppPaymentId(6958)
val badgeExp = "9564321".toLong()
val amount = FiatValue("INR", DecimalValue(7000),20241124092902)
val subId = SubscriberId.generate()
val badgeList1=BadgeList.Badge("BOOST","donor","Signal Boost","{short_name} supports Signal with a monthly donation. Signal is a nonprofit with no advertisers or investors, supported only by people like you.","", 9561256L,true,"180dpi")
val mockInAppPayment = InAppPaymentTable.InAppPayment(
  id = payId,
  data = InAppPaymentData(
    amount = amount, // Example payment of $10
    badge = badgeList1,
    paymentMethodType = InAppPaymentData.PaymentMethodType.CARD,
    level = 1,
    error = null // No error for successful simulation
  ),
  type = InAppPaymentType.ONE_TIME_DONATION,
  notified = false,
  insertedAt = Duration.INFINITE,
  state = InAppPaymentTable.State.END,
  endOfPeriod = Duration.INFINITE ,
  subscriberId = subId,
  updatedAt = Duration.ZERO

)

// Mock InAppPaymentProcessorActionResult
val mockResult = InAppPaymentProcessorActionResult(
  action = InAppPaymentProcessorAction.UPDATE_SUBSCRIPTION,
  inAppPayment = mockInAppPayment,
  inAppPaymentType = InAppPaymentType.ONE_TIME_DONATION,
  status = InAppPaymentProcessorActionResult.Status.SUCCESS
)
