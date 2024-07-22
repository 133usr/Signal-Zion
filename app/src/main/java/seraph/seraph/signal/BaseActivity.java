package seraph.zion.signal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.WalletConstants;

import org.signal.core.util.logging.Log;
import org.signal.core.util.money.FiatMoney;
import org.signal.donations.GooglePayApi;

import seraph.zion.signal.badges.models.Badge;
import seraph.zion.signal.keyvalue.DonationsValues;
import seraph.zion.signal.subscription.Subscriber;
import seraph.zion.signal.util.VolumeDown_Listener;
import seraph.zion.signal.dependencies.ApplicationDependencies;
import seraph.zion.signal.util.AppStartup;
import seraph.zion.signal.util.ConfigurationUtil;
import seraph.zion.signal.util.VolumeDown_Listener;
import seraph.zion.signal.util.WindowUtil;
import seraph.zion.signal.util.dynamiclanguage.DynamicLanguageContextWrapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Base class for all activities. The vast majority of activities shouldn't extend this directly.
 * Instead, they should extend {@link PassphraseRequiredActivity} so they're protected by
 * screen lock.
 */
public abstract class BaseActivity extends AppCompatActivity {
  private static final String TAG                  = Log.tag(BaseActivity.class);
  private static final int GOOGLE_PAY_REQUEST_CODE = 100;

  private GooglePayApi googlePayApi;

  VolumeDown_Listener volumeDown_listener;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    AppStartup.getInstance().onCriticalRenderEventStart();
    logEvent("onCreate()");
    super.onCreate(savedInstanceState);
    volumeDown_listener = new VolumeDown_Listener(this);
    AppStartup.getInstance().onCriticalRenderEventEnd();



    googlePayApi = new GooglePayApi(this, new MyGateway(), new GooglePayApi.Configuration(WalletConstants.ENVIRONMENT_TEST));

    // Check if Google Pay is available and ready
    googlePayApi.queryIsReadyToPay()
                .subscribe(
                    () -> {
                      // Google Pay is ready, proceed with payment
                      simulatePayment();
                    },
                    throwable -> {
                      // Error occurred, Google Pay is not available
                      handleError(throwable);
                    }
                );

    simulateBadgeEarning(this);
  }

  // Simulate a user making a donation and earning a badge
  public static void simulateBadgeEarning(Context context) {
    // Define the criteria for earning a "Donor" badge
    int donationAmount = 50; // Simulated donation amount required to earn the badge
    int userDonationAmount = 50; // Simulated user donation amount

    // Check if the user's donation meets the criteria for earning a badge
    if (userDonationAmount >= donationAmount) {
      // User meets the criteria, create a Donor badge object
      Badge donorBadge = new Badge(
          "DONOR",
          Badge.Category.Donor,
          "Donor Badge",
          "Congratulations! You've earned the Donor Badge.",
          Uri.parse("https://i.ibb.co/bdLpbk3/success.png"),
          "hdpi", // Example image density
          System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000), // Badge expires in 30 days
          true,
          30 * 24 * 60 * 60 * 1000 // Duration of badge validity (30 days)
      );
      Toast.makeText(context, "got it", Toast.LENGTH_SHORT).show();
      // Optionally, you can display the badge to the user in the UI
      displayBadgeToUser(donorBadge);
    } else {
      Toast.makeText(context, "no sory", Toast.LENGTH_SHORT).show();
      // User does not meet the criteria for earning a badge
      System.out.println("Sorry, you have not earned the Donor Badge yet. Please consider making a donation.");
    }
  }

  // Function to display the earned badge to the user
  public static void displayBadgeToUser(Badge badge) {
    // You can implement the logic to display the badge in your app's UI
    System.out.println("Congratulations! You've earned a badge: " + badge.getName());
    System.out.println("Description: " + badge.getDescription());
    System.out.println("Badge Image URL: " + badge.getImageUrl());
  }






  private void simulatePayment() {
    // Simulate a payment with a price of $10 and a label
    FiatMoney price = new FiatMoney(new BigDecimal("10"), Currency.getInstance("USD"));
    String label = "Test Payment";
    googlePayApi.requestPayment(price, label, GOOGLE_PAY_REQUEST_CODE);
  }
  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    // Handle the result of the Google Pay request
    googlePayApi.onActivityResult(requestCode, resultCode, data, GOOGLE_PAY_REQUEST_CODE, new GooglePayApi.PaymentRequestCallback() {
      @Override
      public void onSuccess(PaymentData paymentData) {
        // Payment was successful
        handlePaymentSuccess(paymentData);
      }

      @Override
      public void onError(GooglePayApi.GooglePayException googlePayException) {
        // Error occurred during payment
        handleError(googlePayException);
      }

      @Override
      public void onCancelled() {
        // Payment was cancelled by the user
        handlePaymentCancelled();
      }
    });
  }

  private void handlePaymentSuccess(PaymentData paymentData) {
    // Handle successful payment
    Toast.makeText(this, "Succes", Toast.LENGTH_SHORT).show();
  }

  private void handleError(Throwable throwable) {
    // Handle error
  }

  private void handlePaymentCancelled() {
    // Handle payment cancellation
  }

  // Define your Gateway implementation
  public class MyGateway implements GooglePayApi.Gateway {
    @Override
    public Map<String, String> getTokenizationSpecificationParameters() {
      // Implement tokenization specification parameters
      return Collections.emptyMap();
    }

    @Override
    public List<String> getAllowedCardNetworks() {
      // Define allowed card networks
      return Arrays.asList(
          String.valueOf(WalletConstants.CARD_NETWORK_VISA),
          String.valueOf(WalletConstants.CARD_NETWORK_MASTERCARD)
          // Add more card networks as needed
      );
    }
  }






























  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    return volumeDown_listener.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
  }
  @Override
  protected void onResume() {
    super.onResume();
    WindowUtil.initializeScreenshotSecurity(this, getWindow());
  }

  @Override
  protected void onStart() {
    logEvent("onStart()");
    ApplicationDependencies.getShakeToReport().registerActivity(this);
    super.onStart();
  }

  @Override
  protected void onStop() {
    logEvent("onStop()");
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    logEvent("onDestroy()");
    super.onDestroy();
  }

  protected void startActivitySceneTransition(Intent intent, View sharedView, String transitionName) {
    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this, sharedView, transitionName)
                                         .toBundle();
    ActivityCompat.startActivity(this, intent, bundle);
  }

  @Override
  protected void attachBaseContext(@NonNull Context newBase) {
    super.attachBaseContext(newBase);

    Configuration configuration      = new Configuration(newBase.getResources().getConfiguration());
    int           appCompatNightMode = getDelegate().getLocalNightMode() != AppCompatDelegate.MODE_NIGHT_UNSPECIFIED ? getDelegate().getLocalNightMode()
                                                                                                                     : AppCompatDelegate.getDefaultNightMode();

    configuration.uiMode      = (configuration.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | mapNightModeToConfigurationUiMode(newBase, appCompatNightMode);
    configuration.orientation = Configuration.ORIENTATION_UNDEFINED;

    applyOverrideConfiguration(configuration);
  }

  @Override
  public void applyOverrideConfiguration(@NonNull Configuration overrideConfiguration) {
    DynamicLanguageContextWrapper.prepareOverrideConfiguration(this, overrideConfiguration);
    super.applyOverrideConfiguration(overrideConfiguration);
  }

  private void logEvent(@NonNull String event) {
    Log.d(TAG, "[" + Log.tag(getClass()) + "] " + event);
  }

  public final @NonNull ActionBar requireSupportActionBar() {
    return Objects.requireNonNull(getSupportActionBar());
  }

  private static int mapNightModeToConfigurationUiMode(@NonNull Context context, @AppCompatDelegate.NightMode int appCompatNightMode) {
    if (appCompatNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
      return Configuration.UI_MODE_NIGHT_YES;
    } else if (appCompatNightMode == AppCompatDelegate.MODE_NIGHT_NO) {
      return Configuration.UI_MODE_NIGHT_NO;
    }
    return ConfigurationUtil.getNightModeConfiguration(context.getApplicationContext());
  }
}
