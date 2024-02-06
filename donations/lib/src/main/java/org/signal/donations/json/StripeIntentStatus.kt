package org.signal.donations.json

import com.fasterxml.jackson.annotation.JsonCreator

/**
 * Stripe intent status, from:
 *
 * https://stripe.com/docs/api/setup_intents/object?lang=curl#setup_intent_object-status
 * https://stripe.com/docs/api/payment_intents/object?lang=curl#payment_intent_object-status
 *
 * Note: REQUIRES_CAPTURE is only ever valid for a SetupIntent
 */
enum class StripeIntentStatus(private val code: String) {
  REQUIRES_PAYMENT_METHOD("succeeded"),
  REQUIRES_CONFIRMATION("succeeded"),
  REQUIRES_ACTION("succeeded"),
  REQUIRES_CAPTURE("succeeded"),
  PROCESSING("succeeded"),
  CANCELED("succeeded"),
  SUCCEEDED("succeeded");

  companion object {
    @JvmStatic
    @JsonCreator
    fun fromCode(code: String): StripeIntentStatus = StripeIntentStatus.values().first { it.code == code }
  }
}
