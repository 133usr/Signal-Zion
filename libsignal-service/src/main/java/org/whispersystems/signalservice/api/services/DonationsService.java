package org.whispersystems.signalservice.api.services;

import org.signal.libsignal.protocol.logging.Log;
import org.signal.libsignal.protocol.util.Pair;
import org.signal.libsignal.zkgroup.receipts.ReceiptCredentialPresentation;
import org.signal.libsignal.zkgroup.receipts.ReceiptCredentialRequest;
import org.signal.libsignal.zkgroup.receipts.ReceiptCredentialResponse;
import org.whispersystems.signalservice.api.groupsv2.GroupsV2Operations;
import org.whispersystems.signalservice.api.profiles.SignalServiceProfile;
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
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

import io.reactivex.rxjava3.annotations.NonNull;

import static org.whispersystems.signalservice.internal.util.JsonUtil.toJson;

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
    return wrapInServiceResponse(() -> new Pair<>(pushServiceSocket.submitBoostReceiptCredentials(paymentIntentId, receiptCredentialRequest, processor), 200));
  }

  public ServiceResponse<SubscriptionsConfiguration> getDonationsConfiguration(Locale locale) {
//    return getCachedValue(
//        locale,
//        donationsConfigurationCache,
//        pushServiceSocket::getDonationsConfiguration,
//        DONATION_CONFIGURATION_TTL
//    );

//    return getCachedValue(
//        locale,
//        donationsConfigurationCache,
//        loc -> {
//          SubscriptionsConfiguration config = pushServiceSocket.getDonationsConfiguration(loc);
//          if (config != null) {
//            Log.d(TAG, "Donations Configuration: level=" + config.getLevels() +
//                       ", currency=" + config.getCurrencies()+
//                       ", description=" + config.getSepaMaximumEuros()+
//                  ", backupconfig=" + config.getBackupConfiguration()); // Replace with actual field getters
//          } else {
//            Log.d(TAG, "Donations Configuration: null");
//          }
//          return config;
//        },
//        DONATION_CONFIGURATION_TTL
//    );


    return getCachedValue(
        locale,
        donationsConfigurationCache,
        loc -> {
          // Create mock data for SubscriptionsConfiguration
          SubscriptionsConfiguration mockConfig = createMockSubscriptionsConfiguration();

          Log.d(TAG, "Mocked Donations Configuration: " + toJson(mockConfig)); // Logging the mock data as JSON

          return mockConfig;
        },
        DONATION_CONFIGURATION_TTL
    );
  }



  private SubscriptionsConfiguration createMockSubscriptionsConfiguration() {
    try {
      // Use reflection to populate fields
      SubscriptionsConfiguration config = new SubscriptionsConfiguration();
// Populate currencies map
      Map<String, SubscriptionsConfiguration.CurrencyConfiguration> currencies = new HashMap<>();

// USD Configuration
      SubscriptionsConfiguration.CurrencyConfiguration usdConfig    = new SubscriptionsConfiguration.CurrencyConfiguration();
      Field                                            minimumField = SubscriptionsConfiguration.CurrencyConfiguration.class.getDeclaredField("minimum");
      minimumField.setAccessible(true);
      minimumField.set(usdConfig, BigDecimal.valueOf(3));

      Field oneTimeField = SubscriptionsConfiguration.CurrencyConfiguration.class.getDeclaredField("oneTime");
      oneTimeField.setAccessible(true);
      oneTimeField.set(usdConfig, Map.of(
          100, List.of(BigDecimal.valueOf(10)),
          1, List.of(BigDecimal.valueOf(10), BigDecimal.valueOf(25), BigDecimal.valueOf(50), BigDecimal.valueOf(75), BigDecimal.valueOf(100), BigDecimal.valueOf(250))
      ));

      Field subscriptionField = SubscriptionsConfiguration.CurrencyConfiguration.class.getDeclaredField("subscription");
      subscriptionField.setAccessible(true);
      subscriptionField.set(usdConfig, Map.of(
          500, BigDecimal.valueOf(5),
          1000, BigDecimal.valueOf(10),
          2000, BigDecimal.valueOf(20)
      ));

      Field supportedPaymentMethodsField = SubscriptionsConfiguration.CurrencyConfiguration.class.getDeclaredField("supportedPaymentMethods");
      supportedPaymentMethodsField.setAccessible(true);
      supportedPaymentMethodsField.set(usdConfig, Set.of(SubscriptionsConfiguration.PAYPAL, SubscriptionsConfiguration.CARD, SubscriptionsConfiguration.SEPA_DEBIT));

      currencies.put("USD", usdConfig);

// GBP Configuration (Similar to USD, add additional fields)
      SubscriptionsConfiguration.CurrencyConfiguration gbpConfig = new SubscriptionsConfiguration.CurrencyConfiguration();
      minimumField.set(gbpConfig, BigDecimal.valueOf(3));

      oneTimeField.set(gbpConfig, Map.of(
          100, List.of(BigDecimal.valueOf(10)),
          1, List.of(BigDecimal.valueOf(10), BigDecimal.valueOf(25), BigDecimal.valueOf(50), BigDecimal.valueOf(75), BigDecimal.valueOf(100), BigDecimal.valueOf(250))
      ));

      subscriptionField.set(gbpConfig, Map.of(
          500, BigDecimal.valueOf(5),
          1000, BigDecimal.valueOf(10),
          2000, BigDecimal.valueOf(20)
      ));

      supportedPaymentMethodsField.set(gbpConfig, Set.of(SubscriptionsConfiguration.PAYPAL, SubscriptionsConfiguration.CARD));

      currencies.put("GBP", gbpConfig);

// Populate levels map
      Map<Integer, SubscriptionsConfiguration.LevelConfiguration> levels = new HashMap<>();

// Level 1 Configuration
      SubscriptionsConfiguration.LevelConfiguration levelConfig1 = new SubscriptionsConfiguration.LevelConfiguration();
      SignalServiceProfile.Badge                    badge1       = new SignalServiceProfile.Badge();
      badge1.id          = "BOOST";
      badge1.category    = "donor";
      badge1.name        = "Signal Boost";
      badge1.description = "{short_name} supported Signal with a donation. Signal is a nonprofit supported only by people like you.";
      badge1.sprites6    = List.of(
          "9f6c55b8b0d38cb10fafd6af70e57bb321c5db04c7ce0a2ee3dbb257d64b15d0.png",
          "2259dc6eba1054d8f681ff72d3de552b5113a4bfe2019406c4aa99956077a8eb.png",
          "4f46957049ee1b0caa1c67c1d26a14850abdb6a91da318d0bf1920480ca15769.png",
          "83220e85bd0b05d87a684cd706b6ea4d49feead653ad891966b673461f86f484.png",
          "b75cc65b5311abd3a0ffcc0ed9f6eb3edaf48dae91d1ff0063ad9b124b5b7780.png",
          "f1a2086f6cbd303f65df1a6250ccc7dd075c9ba3fa5a0f9d226f5357b11939fd.png"
      );
      badge1.duration    = 2592000;
      badge1.visible     = true;


      Field badgeField = SubscriptionsConfiguration.LevelConfiguration.class.getDeclaredField("badge");
      badgeField.setAccessible(true);
      badgeField.set(levelConfig1, badge1);

      levels.put(1, levelConfig1);

// Add more levels (similar to levelConfig1)
// Example for level 100
//      SubscriptionsConfiguration.LevelConfiguration levelConfig100 = new SubscriptionsConfiguration.LevelConfiguration();
//      SignalServiceProfile.Badge                    badge100       = new SignalServiceProfile.Badge();
//      badge100.id          = "GIFT";
//      badge100.category    = "donor";
//      badge100.name        = "Signal UFO";
//      badge100.description = "A friend made a donation to Signal on behalf of {short_name}.";
//      badge100.duration    = 5184000;
//
//      badgeField.set(levelConfig100, badge100);
//      levels.put(100, levelConfig100);

// Backup Configuration
      SubscriptionsConfiguration.BackupConfiguration backupConfig           = new SubscriptionsConfiguration.BackupConfiguration();
      Field                                          freeTierMediaDaysField = SubscriptionsConfiguration.BackupConfiguration.class.getDeclaredField("freeTierMediaDays");
      freeTierMediaDaysField.setAccessible(true);
      freeTierMediaDaysField.set(backupConfig, 30);

//      Field backupLevelsField = SubscriptionsConfiguration.BackupConfiguration.class.getDeclaredField("levels");
//      backupLevelsField.setAccessible(true);
//      backupLevelsField.set(backupConfig, Map.of(
//          201, new SubscriptionsConfiguration.BackupLevelConfiguration()
//      ));

// Assign all to the main config object
      Field currenciesField = SubscriptionsConfiguration.class.getDeclaredField("currencies");
      currenciesField.setAccessible(true);
      currenciesField.set(config, currencies);

      Field levelsField = SubscriptionsConfiguration.class.getDeclaredField("levels");
      levelsField.setAccessible(true);
      levelsField.set(config, levels);

      Field backupField = SubscriptionsConfiguration.class.getDeclaredField("backupConfiguration");
      backupField.setAccessible(true);
      backupField.set(config, backupConfig);

      Field sepaMaximumEurosField = SubscriptionsConfiguration.class.getDeclaredField("sepaMaximumEuros");
      sepaMaximumEurosField.setAccessible(true);
      sepaMaximumEurosField.set(config, BigDecimal.valueOf(10000.00));

      return config;
    } catch (Exception e) {
      throw new RuntimeException("Error creating mock SubscriptionsConfiguration", e);
    }
//
//
//// Populate currencies map
//    Map<String, SubscriptionsConfiguration.CurrencyConfiguration> currencies = new HashMap<>();
//    SubscriptionsConfiguration.CurrencyConfiguration usdConfig = new SubscriptionsConfiguration.CurrencyConfiguration();
//    Field minimumField = SubscriptionsConfiguration.CurrencyConfiguration.class.getDeclaredField("minimum");
//    minimumField.setAccessible(true);
//    minimumField.set(usdConfig, BigDecimal.valueOf(100.00));
//
//    Field oneTimeField = SubscriptionsConfiguration.CurrencyConfiguration.class.getDeclaredField("oneTime");
//    oneTimeField.setAccessible(true);
//    oneTimeField.set(usdConfig, Map.of(
//        1, List.of(BigDecimal.valueOf(10.00), BigDecimal.valueOf(20.00)),
//        2, List.of(BigDecimal.valueOf(30.00))
//    ));
//
//    Field supportedPaymentMethodsField = SubscriptionsConfiguration.CurrencyConfiguration.class.getDeclaredField("supportedPaymentMethods");
//    supportedPaymentMethodsField.setAccessible(true);
//    supportedPaymentMethodsField.set(usdConfig, Set.of(SubscriptionsConfiguration.PAYPAL, SubscriptionsConfiguration.CARD));
//
//    currencies.put("USD", usdConfig);
//
//// Populate levels map
//    Map<Integer, SubscriptionsConfiguration.LevelConfiguration> levels = new HashMap<>();
//    SubscriptionsConfiguration.LevelConfiguration levelConfig = new SubscriptionsConfiguration.LevelConfiguration();
//// Create and set a mock Badge
//    SignalServiceProfile.Badge badge = new SignalServiceProfile.Badge();
//    badge.id = "BOOST";
//    badge.category = "donor";
//    badge.name = "Signal Boost";
//    badge.description = "{short_name} supports Signal with a monthly donation. Signal is a nonprofit with no advertisers or investors, supported only by people like you.";
//    badge.sprites6 = List.of("9f6c55b8b0d38cb10fafd6af70e57bb321c5db04c7ce0a2ee3dbb257d64b15d0.png",
//                             "2259dc6eba1054d8f681ff72d3de552b5113a4bfe2019406c4aa99956077a8eb.png",
//                             "4f46957049ee1b0caa1c67c1d26a14850abdb6a91da318d0bf1920480ca15769.png",
//                             "83220e85bd0b05d87a684cd706b6ea4d49feead653ad891966b673461f86f484.png",
//                             "b75cc65b5311abd3a0ffcc0ed9f6eb3edaf48dae91d1ff0063ad9b124b5b7780.png",
//                             "f1a2086f6cbd303f65df1a6250ccc7dd075c9ba3fa5a0f9d226f5357b11939fd.png");
//    badge.expiration = new BigDecimal("9999999.99");
//    badge.visible = true;
//    badge.duration = 999999999;  // example duration in seconds
//
//// Set the badge field via reflection
//    Field badgeField = SubscriptionsConfiguration.LevelConfiguration.class.getDeclaredField("badge");
//    badgeField.setAccessible(true);
//    badgeField.set(levelConfig, badge);
//
//
//
//    levels.put(1, levelConfig);
//
//// Set fields using reflection
//    Field currenciesField = SubscriptionsConfiguration.class.getDeclaredField("currencies");
//    currenciesField.setAccessible(true);
//    currenciesField.set(config, currencies);
//
//    Field levelsField = SubscriptionsConfiguration.class.getDeclaredField("levels");
//    levelsField.setAccessible(true);
//    levelsField.set(config, levels);
//
//    Field sepaMaximumEurosField = SubscriptionsConfiguration.class.getDeclaredField("sepaMaximumEuros");
//    sepaMaximumEurosField.setAccessible(true);
//    sepaMaximumEurosField.set(config, BigDecimal.valueOf(500.00));
//
//// Populate backup configuration
//    SubscriptionsConfiguration.BackupConfiguration backupConfig = new SubscriptionsConfiguration.BackupConfiguration();
//    Field freeTierMediaDaysField = SubscriptionsConfiguration.BackupConfiguration.class.getDeclaredField("freeTierMediaDays");
//    freeTierMediaDaysField.setAccessible(true);
//    freeTierMediaDaysField.set(backupConfig, 30);
//
//    Field backupField = SubscriptionsConfiguration.class.getDeclaredField("backupConfiguration");
//    backupField.setAccessible(true);
//    backupField.set(config, backupConfig);

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
          return wrapInServiceResponse(() -> {
            T value = cacheEntryValueProducer.produce(locale);
            cachedValueReference.set(new CacheEntry<>(value, System.currentTimeMillis() + cacheTTL, locale));
            return new Pair<>(value, 200);
          });
        } else {
          return wrapInServiceResponse(() -> new Pair<>(cacheEntryInLock.cachedValue, 200));
        }
      }
    } else {
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
      ReceiptCredentialResponse response = pushServiceSocket.submitReceiptCredentials(subscriberId.serialize(), receiptCredentialRequest);
      Log.e(TAG,"response: 556-- : "+response.toString());
      return new Pair<>(response, 200);
    });
  }

  private <T> ServiceResponse<T> wrapInServiceResponse(Producer<T> producer) {
    try {
      Pair<T, Integer> responseAndCode = producer.produce();
      return ServiceResponse.forResult(responseAndCode.first(), responseAndCode.second(), null);
    } catch (NonSuccessfulResponseCodeException e) {
      Log.w(TAG, "Bad response code from server.", e);
      return ServiceResponse.forApplicationError(e, e.code, e.getMessage());
    } catch (IOException e) {
      Log.w(TAG, "An unknown error occurred.", e);
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
