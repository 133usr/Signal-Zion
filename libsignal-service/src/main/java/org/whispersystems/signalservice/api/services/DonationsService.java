package org.whispersystems.signalservice.api.services;


import org.signal.libsignal.protocol.logging.Log;
import org.signal.libsignal.protocol.util.Pair;
import org.signal.libsignal.zkgroup.InvalidInputException;
import org.signal.libsignal.zkgroup.receipts.ReceiptCredentialPresentation;
import org.signal.libsignal.zkgroup.receipts.ReceiptCredentialRequest;
import org.signal.libsignal.zkgroup.receipts.ReceiptCredentialResponse;
import org.whispersystems.signalservice.api.groupsv2.GroupsV2Operations;
import org.whispersystems.signalservice.api.push.exceptions.NonSuccessfulResponseCodeException;
import org.whispersystems.signalservice.api.subscriptions.ActiveSubscription;
import org.whispersystems.signalservice.api.subscriptions.PayPalConfirmPaymentIntentResponse;
import org.whispersystems.signalservice.api.subscriptions.PayPalCreatePaymentIntentResponse;
import org.whispersystems.signalservice.api.subscriptions.PayPalCreatePaymentMethodResponse;
import org.whispersystems.signalservice.api.subscriptions.StripeClientSecret;
import org.whispersystems.signalservice.api.subscriptions.SubscriberId;
import org.whispersystems.signalservice.api.util.CredentialsProvider;
import org.whispersystems.signalservice.internal.EmptyResponse;
import org.whispersystems.signalservice.internal.ServiceResponse;
import org.whispersystems.signalservice.internal.configuration.SignalServiceConfiguration;
import org.whispersystems.signalservice.internal.push.BankMandate;
import org.whispersystems.signalservice.internal.push.DonationProcessor;
import org.whispersystems.signalservice.internal.push.SubscriptionsConfiguration;
import org.whispersystems.signalservice.internal.push.PushServiceSocket;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

import io.reactivex.rxjava3.annotations.NonNull;


/**
 * One-stop shop for Signal service calls related to donations.
 */
public class DonationsService {

  private static final long DONATION_CONFIGURATION_TTL = TimeUnit.HOURS.toMillis(1);
  private static final long SEPA_DEBIT_MANDATE_TTL = TimeUnit.DAYS.toMillis(1);

  private static final String TAG = DonationsService.class.getSimpleName();

  private final PushServiceSocket pushServiceSocket;

  private final AtomicReference<CacheEntry<SubscriptionsConfiguration>> donationsConfigurationCache = new AtomicReference<>(null);
  private final AtomicReference<CacheEntry<BankMandate>>                sepaBankMandateCache        = new AtomicReference<>(null);

  private static class CacheEntry<T> {
    private final T      cachedValue;
    private final long   expiresAt;
    private final Locale locale;

    private CacheEntry(T cachedValue, long expiresAt, Locale locale) {
      this.cachedValue = cachedValue;
      this.expiresAt   = expiresAt;
      this.locale      = locale;
    }
  }

  public DonationsService(@NonNull PushServiceSocket pushServiceSocket) {
    this.pushServiceSocket = pushServiceSocket;
  }

  /**
   * Allows a user to redeem a given receipt they were given after submitting a donation successfully.
   *
   * @param receiptCredentialPresentation Receipt
   * @param visible                       Whether the badge will be visible on the user's profile immediately after redemption
   * @param primary                       Whether the badge will be made primary immediately after redemption
   */
  public ServiceResponse<EmptyResponse> redeemDonationReceipt(ReceiptCredentialPresentation receiptCredentialPresentation, boolean visible, boolean primary) {
    try {
      pushServiceSocket.redeemDonationReceipt(receiptCredentialPresentation, visible, primary);
      return ServiceResponse.forResult(EmptyResponse.INSTANCE, 200, null);
    } catch (Exception e) {
      return ServiceResponse.<EmptyResponse>forUnknownError(e);
    }
  }

  /**
   * Allows a user to redeem a given receipt they were given after submitting a donation successfully.
   *
   * @param receiptCredentialPresentation Receipt
   */
  public ServiceResponse<EmptyResponse> redeemArchivesReceipt(ReceiptCredentialPresentation receiptCredentialPresentation) {
    try {
      pushServiceSocket.redeemArchivesReceipt(receiptCredentialPresentation);
      return ServiceResponse.forResult(EmptyResponse.INSTANCE, 200, null);
    } catch (Exception e) {
      return ServiceResponse.<EmptyResponse>forUnknownError(e);
    }
  }

  /**
   * Submits price information to the server to generate a payment intent via the payment gateway.
   *
   * @param amount       Price, in the minimum currency unit (e.g. cents or yen)
   * @param currencyCode The currency code for the amount
   * @return A ServiceResponse containing a DonationIntentResult with details given to us by the payment gateway.
   */
  public ServiceResponse<StripeClientSecret> createDonationIntentWithAmount(String amount, String currencyCode, long level, String paymentMethod) {
    return wrapInServiceResponse(() -> new Pair<>(pushServiceSocket.createStripeOneTimePaymentIntent(currencyCode, paymentMethod, Long.parseLong(amount), level), 200));
  }

  /**
   * Given a completed payment intent and a receipt credential request produces a receipt credential response.
   * Clients should always use the same ReceiptCredentialRequest with the same payment intent id. This request is repeatable so long as the two values are reused.
   *
   * @param paymentIntentId          PaymentIntent ID from a boost donation intent response.
   * @param receiptCredentialRequest Client-generated request token
   */
  public ServiceResponse<ReceiptCredentialResponse> submitBoostReceiptCredentialRequestSync(String paymentIntentId, ReceiptCredentialRequest receiptCredentialRequest, DonationProcessor processor) {
    Log.e(TAG,"BEFORE ASKING submitBoostReceiptCredentialRequestSync");
    Log.e(TAG,"LOG paymentIntentId "+paymentIntentId+"\n ReceiptCredentialRequest "+receiptCredentialRequest.toString()+"\n DonationProcessor "+processor.toString());
    return wrapInServiceResponse(() -> new Pair<>(pushServiceSocket.submitBoostReceiptCredentials(paymentIntentId, receiptCredentialRequest, processor), 200));
  }


  //todo mocking the submitbostReceiptcred REquessync

//  public ServiceResponse<ReceiptCredentialResponse> submitBoostReceiptCredentialRequestSync(String paymentIntentId, ReceiptCredentialRequest receiptCredentialRequest, DonationProcessor processor) throws InvalidInputException {
//    Log.e(TAG,"BEFORE ASKING submitBoostReceiptCredentialRequestSync");
//    Log.e(TAG,"LOG paymentIntentId "+paymentIntentId+"\n ReceiptCredentialRequest "+receiptCredentialRequest.toString()+"\n DonationProcessor "+processor.toString());
//    // Mocking ReceiptCredentialRequest
//
//
//    // Create mock response for the method that makes the actual service call
//    ReceiptCredentialResponse mockResponse = Mockito.mock(ReceiptCredentialResponse.class);
//
////    ReceiptCredentialResponse mockResponse = new ReceiptCredentialResponse(new byte[]{});
//
//    // Wrap the mocked response in ServiceResponse and return it
//    // Wrap the mocked response in a Pair and pass it to wrapInServiceResponse
//    return wrapInServiceResponse(() -> new Pair<>(mockResponse, 200));
//  }



  public ServiceResponse<SubscriptionsConfiguration> getDonationsConfiguration(Locale locale) {
    return getCachedValue(
        locale,
        donationsConfigurationCache,
        pushServiceSocket::getDonationsConfiguration,
        DONATION_CONFIGURATION_TTL
    );
  }

  public ServiceResponse<BankMandate> getBankMandate(Locale locale, String bankTransferType) {
    return getCachedValue(
        locale,
        sepaBankMandateCache,
        l -> pushServiceSocket.getBankMandate(l, bankTransferType),
        SEPA_DEBIT_MANDATE_TTL
    );
  }

  private <T> ServiceResponse<T> getCachedValue(Locale locale,
                                                AtomicReference<CacheEntry<T>> cachedValueReference,
                                                CacheEntryValueProducer<T> cacheEntryValueProducer,
                                                long cacheTTL
  )
  {
    CacheEntry<T> cacheEntryOutsideLock = cachedValueReference.get();
    if (isNewCacheEntryRequired(cacheEntryOutsideLock, locale)) {
      synchronized (this) {
        CacheEntry<T> cacheEntryInLock = cachedValueReference.get();
        if (isNewCacheEntryRequired(cacheEntryInLock, locale)) {
          Log.e(TAG,"-------------- RETURN WRAPINSERVICERESPONSE 1 ---------------:");
          return wrapInServiceResponse(() -> {
            T value = cacheEntryValueProducer.produce(locale);
            cachedValueReference.set(new CacheEntry<>(value, System.currentTimeMillis() + cacheTTL, locale));
            Log.e(TAG,"-------------- New Pair value ---------------:" +new Pair<>(value,200));
            return new Pair<>(value, 200);
          });
        } else {
          Log.e(TAG,"-------------- RETURN WRAPINSERVICERESPONSE 2 ---------------:");
          return wrapInServiceResponse(() -> new Pair<>(cacheEntryInLock.cachedValue, 200));
        }
      }
    } else {
      Log.e(TAG,"-------------- RETURN WRAPINSERVICERESPONSE 3 ---------------:");
      return wrapInServiceResponse(() -> new Pair<>(cacheEntryOutsideLock.cachedValue, 200));
    }
  }

  /**
   * Updates the current subscription to the given level and currency. The idempotency key should be a randomly generated 16-byte value that's
   * url-safe-base64-encoded by the client for each user-operation. That is, if the user is updating from level 500 to level 1000 and the client has to retry
   * the request, the idempotency key should remain the same. However, if the user updates from level 500 to level 1000, then updates from level 1000 to
   * level 500, then updates from level 500 to level 1000 again all three of these operations should have separate idempotency keys. Think of this value as an
   * indicator of user-intention. It should be the same for retries, but any new user-intention to update the subscription should produce a unique value.
   *
   * @param subscriberId   The subscriber ID for the user changing their subscription level
   * @param level          The new level to subscribe to
   * @param currencyCode   The currencyCode the user is using for payment
   * @param idempotencyKey url-safe-base64-encoded random 16-byte value (see description)
   * @param lock           A lock to lock on to avoid a situation where this subscription update happens *as* we are trying to get a credential receipt.
   */
  public ServiceResponse<EmptyResponse> updateSubscriptionLevel(SubscriberId subscriberId,
                                                                String level,
                                                                String currencyCode,
                                                                String idempotencyKey,
                                                                Lock lock
  )
  {
    return wrapInServiceResponse(() -> {
      lock.lock();

      try {
        pushServiceSocket.updateSubscriptionLevel(subscriberId.serialize(), level, currencyCode, idempotencyKey);
      } finally {
        lock.unlock();
      }

      return new Pair<>(EmptyResponse.INSTANCE, 200);
    });
  }

  public ServiceResponse<EmptyResponse> linkGooglePlayBillingPurchaseTokenToSubscriberId(SubscriberId subscriberId, String purchaseToken, Lock lock) {
    return wrapInServiceResponse(() -> {
      lock.lock();
      try {
        pushServiceSocket.linkPlayBillingPurchaseToken(subscriberId.serialize(), purchaseToken);
      } finally {
        lock.unlock();
      }

      return new Pair<>(EmptyResponse.INSTANCE, 200);
    });
  }

  /**
   * Synchronously returns information about the current subscription if one exists.
   */
  public ServiceResponse<ActiveSubscription> getSubscription(SubscriberId subscriberId) {
    return wrapInServiceResponse(() -> {
      ActiveSubscription response = pushServiceSocket.getSubscription(subscriberId.serialize());
      return new Pair<>(response, 200);
    });
  }

  /**
   * Creates a subscriber record on the signal server and stripe. Can be called idempotently as-is. After receiving 200 from this endpoint,
   * clients should save subscriberId locally and to storage service for the account. If you get a 403 from this endpoint and you did not
   * use an account authenticated connection, then the subscriberId has been corrupted in some way.
   * <p>
   * Clients MUST periodically hit this endpoint to update the access time on the subscription record. Recommend trying to call it approximately
   * every 3 days. Not accessing this endpoint for an extended period of time will result in the subscription being canceled.
   *
   * @param subscriberId The subscriber ID for the user polling their subscription
   */
  public ServiceResponse<EmptyResponse> putSubscription(SubscriberId subscriberId) {
    return wrapInServiceResponse(() -> {
      pushServiceSocket.putSubscription(subscriberId.serialize());
      return new Pair<>(EmptyResponse.INSTANCE, 200);
    });
  }

  /**
   * Cancels any current subscription at the end of the current subscription period.
   *
   * @param subscriberId The subscriber ID for the user cancelling their subscription
   */
  public ServiceResponse<EmptyResponse> cancelSubscription(SubscriberId subscriberId) {
    return wrapInServiceResponse(() -> {
      pushServiceSocket.deleteSubscription(subscriberId.serialize());
      return new Pair<>(EmptyResponse.INSTANCE, 200);
    });
  }

  public ServiceResponse<EmptyResponse> setDefaultStripePaymentMethod(SubscriberId subscriberId, String paymentMethodId) {
    return wrapInServiceResponse(() -> {
      pushServiceSocket.setDefaultStripeSubscriptionPaymentMethod(subscriberId.serialize(), paymentMethodId);
      return new Pair<>(EmptyResponse.INSTANCE, 200);
    });
  }

  public ServiceResponse<EmptyResponse> setDefaultIdealPaymentMethod(SubscriberId subscriberId, String setupIntentId) {
    return wrapInServiceResponse(() -> {
      pushServiceSocket.setDefaultIdealSubscriptionPaymentMethod(subscriberId.serialize(), setupIntentId);
      return new Pair<>(EmptyResponse.INSTANCE, 200);
    });
  }

  /**
   * @param subscriberId The subscriber ID to create a payment method for.
   * @return Client secret for a SetupIntent. It should not be used with the PaymentIntent stripe APIs
   * but instead with the SetupIntent stripe APIs.
   */
  public ServiceResponse<StripeClientSecret> createStripeSubscriptionPaymentMethod(SubscriberId subscriberId, String type) {
    return wrapInServiceResponse(() -> {
      StripeClientSecret clientSecret = pushServiceSocket.createStripeSubscriptionPaymentMethod(subscriberId.serialize(), type);
      return new Pair<>(clientSecret, 200);
    });
  }

  /**
   * Creates a PayPal one-time payment and returns the approval URL
   * Response Codes
   * 200 - success
   * 400 - request error
   * 409 - level requires a valid currency/amount combination that does not match
   *
   * @param locale       User locale for proper language presentation
   * @param currencyCode 3 letter currency code of the desired currency
   * @param amount       Stringified minimum precision amount
   * @param level        The badge level to purchase
   * @param returnUrl    The 'return' url after a successful login and confirmation
   * @param cancelUrl    The 'cancel' url for a cancelled confirmation
   * @return Wrapped response with either an error code or a payment id and approval URL
   */
  public ServiceResponse<PayPalCreatePaymentIntentResponse> createPayPalOneTimePaymentIntent(Locale locale,
                                                                                             String currencyCode,
                                                                                             String amount,
                                                                                             long level,
                                                                                             String returnUrl,
                                                                                             String cancelUrl)
  {
    return wrapInServiceResponse(() -> {
      PayPalCreatePaymentIntentResponse response = pushServiceSocket.createPayPalOneTimePaymentIntent(
          locale,
          currencyCode.toUpperCase(Locale.US), // Chris Eager to make this case insensitive in the next build
          Long.parseLong(amount),
          level,
          returnUrl,
          cancelUrl
      );
      return new Pair<>(response, 200);
    });
  }

  /**
   * Confirms a PayPal one-time payment and returns the paymentId for receipt credentials
   * Response Codes
   * 200 - success
   * 400 - request error
   * 409 - level requires a valid currency/amount combination that does not match
   *
   * @param currency     3 letter currency code of the desired currency
   * @param amount       Stringified minimum precision amount
   * @param level        The badge level to purchase
   * @param payerId      Passed as a URL parameter back to returnUrl
   * @param paymentId    Passed as a URL parameter back to returnUrl
   * @param paymentToken Passed as a URL parameter back to returnUrl
   * @return Wrapped response with either an error code or a payment id
   */
  public ServiceResponse<PayPalConfirmPaymentIntentResponse> confirmPayPalOneTimePaymentIntent(String currency,
                                                                                               String amount,
                                                                                               long level,
                                                                                               String payerId,
                                                                                               String paymentId,
                                                                                               String paymentToken)
  {
    return wrapInServiceResponse(() -> {
      PayPalConfirmPaymentIntentResponse response = pushServiceSocket.confirmPayPalOneTimePaymentIntent(currency, amount, level, payerId, paymentId, paymentToken);
      Log.e("Response code in donation servie;", String.valueOf(response));
      return new Pair<>(response, 200);
    });

  }

  /**
   * Sets up a payment method via PayPal for recurring charges.
   * <p>
   * Response Codes
   * 200 - success
   * 403 - subscriberId password mismatches OR account authentication is present
   * 404 - subscriberId is not found or malformed
   *
   * @param locale       User locale
   * @param subscriberId User subscriber id
   * @param returnUrl    A success URL
   * @param cancelUrl    A cancel URL
   * @return A response with an approval url and token
   */
  public ServiceResponse<PayPalCreatePaymentMethodResponse> createPayPalPaymentMethod(Locale locale,
                                                                                      SubscriberId subscriberId,
                                                                                      String returnUrl,
                                                                                      String cancelUrl)
  {
    return wrapInServiceResponse(() -> {
      PayPalCreatePaymentMethodResponse response = pushServiceSocket.createPayPalPaymentMethod(locale, subscriberId.serialize(), returnUrl, cancelUrl);
      return new Pair<>(response, 200);
    });
  }

  /**
   * Sets the given payment method as the default in PayPal
   * <p>
   * Response Codes
   * 200 - success
   * 403 - subscriberId password mismatches OR account authentication is present
   * 404 - subscriberId is not found or malformed
   * 409 - subscriber record is missing customer ID - must call POST /v1/subscription/{subscriberId}/create_payment_method first
   *
   * @param subscriberId    User subscriber id
   * @param paymentMethodId Payment method id to make default
   */
  public ServiceResponse<EmptyResponse> setDefaultPayPalPaymentMethod(SubscriberId subscriberId, String paymentMethodId) {
    return wrapInServiceResponse(() -> {
      pushServiceSocket.setDefaultPaypalSubscriptionPaymentMethod(subscriberId.serialize(), paymentMethodId);
      return new Pair<>(EmptyResponse.INSTANCE, 200);
    });
  }

  public ServiceResponse<ReceiptCredentialResponse> submitReceiptCredentialRequestSync(SubscriberId subscriberId, ReceiptCredentialRequest receiptCredentialRequest) {
    return wrapInServiceResponse(() -> {
      Log.e(TAG,"submitReceiptCredentialRequestSync is asking response for SubscriberId "+subscriberId.serialize()+" and receipt request "+receiptCredentialRequest.toString());
      ReceiptCredentialResponse response = pushServiceSocket.submitReceiptCredentials(subscriberId.serialize(), receiptCredentialRequest);
      Log.e(TAG,"-------------- response from wrapInservic ---------------:"+response.toString());
      return new Pair<>(response, 200);
    });
  }

  private <T> ServiceResponse<T> wrapInServiceResponse(Producer<T> producer) {
    try {
      Pair<T, Integer> responseAndCode = producer.produce();
      Log.e(TAG,"-------###------- RESPONSE CODE FIRST: "+responseAndCode.first()+" AND SECOND: "+responseAndCode.second());
      return ServiceResponse.forResult(responseAndCode.first(), responseAndCode.second(), null);
    } catch (NonSuccessfulResponseCodeException e) {
      Log.e(TAG, "Bad response code from server.", e);
      return ServiceResponse.forApplicationError(e, e.code, e.getMessage());
    } catch (IOException e) {
      Log.e(TAG, "An unknown error occurred.", e);
      return ServiceResponse.forUnknownError(e);
    }
  }

  private <T> boolean isNewCacheEntryRequired(CacheEntry<T> cacheEntry, Locale locale) {
    return cacheEntry == null || cacheEntry.expiresAt < System.currentTimeMillis() || !Objects.equals(locale, cacheEntry.locale);
  }

  private interface Producer<T> {
    Pair<T, Integer> produce() throws IOException;
  }

  interface CacheEntryValueProducer<T> {
    T produce(Locale locale) throws IOException;
  }
}
