/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.backup.v2.ui

import seraph.zion.signal.components.settings.app.subscription.donate.gateway.GatewayResponse
import seraph.zion.signal.keyvalue.SignalStore
import seraph.zion.signal.lock.v2.PinKeyboardType

data class MessageBackupsFlowState(
  val selectedMessageBackupsType: MessageBackupsType? = null,
  val availableBackupsTypes: List<MessageBackupsType> = emptyList(),
  val selectedPaymentGateway: GatewayResponse.Gateway? = null,
  val availablePaymentGateways: List<GatewayResponse.Gateway> = emptyList(),
  val pin: String = "",
  val pinKeyboardType: PinKeyboardType = SignalStore.pinValues().keyboardType
)
