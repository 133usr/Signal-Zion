package org.thoughtcrime.securesms.components.settings.app.subscription.donate

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.navGraphViewModels
import com.google.android.gms.wallet.PaymentData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import org.signal.core.util.concurrent.LifecycleDisposable
import org.signal.core.util.getParcelableCompat
import org.signal.core.util.logging.Log
import org.signal.donations.GooglePayApi
import org.signal.donations.InAppPaymentType
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.settings.app.subscription.DonationSerializationHelper.toFiatMoney
import org.thoughtcrime.securesms.components.settings.app.subscription.InAppDonations
import org.thoughtcrime.securesms.components.settings.app.subscription.InAppPaymentComponent
import org.thoughtcrime.securesms.components.settings.app.subscription.InAppPaymentsRepository
import org.thoughtcrime.securesms.components.settings.app.subscription.InAppPaymentsRepository.toPaymentSourceType
import org.thoughtcrime.securesms.components.settings.app.subscription.donate.DonateToSignalFragment.Companion
import org.thoughtcrime.securesms.components.settings.app.subscription.donate.card.CreditCardFragment
import org.thoughtcrime.securesms.components.settings.app.subscription.donate.paypal.PayPalPaymentInProgressFragment
import org.thoughtcrime.securesms.components.settings.app.subscription.donate.stripe.StripePaymentInProgressFragment
import org.thoughtcrime.securesms.components.settings.app.subscription.donate.stripe.StripePaymentInProgressViewModel
import org.thoughtcrime.securesms.components.settings.app.subscription.donate.transfer.BankTransferRequestKeys
import org.thoughtcrime.securesms.components.settings.app.subscription.errors.DonationError
import org.thoughtcrime.securesms.components.settings.app.subscription.errors.DonationErrorDialogs
import org.thoughtcrime.securesms.components.settings.app.subscription.errors.DonationErrorParams
import org.thoughtcrime.securesms.database.InAppPaymentTable
import org.thoughtcrime.securesms.database.SignalDatabase
import org.thoughtcrime.securesms.database.model.databaseprotos.InAppPaymentData
import org.thoughtcrime.securesms.util.fragments.requireListener
import org.whispersystems.signalservice.api.subscriptions.SubscriberId
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Abstracts out some common UI-level interactions between gift flow and normal donate flow.
 */
class InAppPaymentCheckoutDelegate(
  private val fragment: Fragment,
  private val callback: Callback,
  inAppPaymentIdSource: Flowable<InAppPaymentTable.InAppPaymentId>
) : DefaultLifecycleObserver {

  companion object {
    private val TAG = Log.tag(InAppPaymentCheckoutDelegate::class.java)
  }

  private val inAppPaymentComponent: InAppPaymentComponent by lazy { fragment.requireListener() }
  private val disposables = LifecycleDisposable()
  private val viewModel: DonationCheckoutViewModel by fragment.viewModels()

  private val stripePaymentViewModel: StripePaymentInProgressViewModel by fragment.navGraphViewModels(
    R.id.checkout_flow,
    factoryProducer = {
      StripePaymentInProgressViewModel.Factory(inAppPaymentComponent.stripeRepository)
    }
  )

  init {
    fragment.viewLifecycleOwner.lifecycle.addObserver(this)
    ErrorHandler().attach(fragment, callback, inAppPaymentIdSource)

  }

  override fun onCreate(owner: LifecycleOwner) {
    disposables.bindTo(fragment.viewLifecycleOwner)
    registerGooglePayCallback()

    fragment.setFragmentResultListener(StripePaymentInProgressFragment.REQUEST_KEY) { _, bundle ->
      val result: InAppPaymentProcessorActionResult = bundle.getParcelableCompat(StripePaymentInProgressFragment.REQUEST_KEY, InAppPaymentProcessorActionResult::class.java)!!
      handleDonationProcessorActionResult(result)
    }

    fragment.setFragmentResultListener(CreditCardFragment.REQUEST_KEY) { _, bundle ->
      val result: InAppPaymentProcessorActionResult = bundle.getParcelableCompat(StripePaymentInProgressFragment.REQUEST_KEY, InAppPaymentProcessorActionResult::class.java)!!
      handleDonationProcessorActionResult(result)
    }

    fragment.setFragmentResultListener(BankTransferRequestKeys.REQUEST_KEY) { _, bundle ->
      val result: InAppPaymentProcessorActionResult = bundle.getParcelableCompat(StripePaymentInProgressFragment.REQUEST_KEY, InAppPaymentProcessorActionResult::class.java)!!
      handleDonationProcessorActionResult(result)
    }

    fragment.setFragmentResultListener(BankTransferRequestKeys.PENDING_KEY) { _, bundle ->
      val request: InAppPaymentTable.InAppPayment = bundle.getParcelableCompat(BankTransferRequestKeys.PENDING_KEY, InAppPaymentTable.InAppPayment::class.java)!!
      callback.navigateToDonationPending(inAppPayment = request)
    }

    fragment.setFragmentResultListener(PayPalPaymentInProgressFragment.REQUEST_KEY) { _, bundle ->
      val result: InAppPaymentProcessorActionResult = bundle.getParcelableCompat(PayPalPaymentInProgressFragment.REQUEST_KEY, InAppPaymentProcessorActionResult::class.java)!!
      handleDonationProcessorActionResult(result)
    }

//    MaterialAlertDialogBuilder(fragment.requireContext())
//      .setTitle("Trying to get a badge")
//      .setMessage("Cross your fingers!!")
//      .setPositiveButton(android.R.string.ok) { _, _ ->
//        callback.onPaymentComplete(mockResult.inAppPayment!!)
//      }.show()
  }

  fun handleGatewaySelectionResponse(inAppPayment: InAppPaymentTable.InAppPayment) {
    if (InAppDonations.isDonationsPaymentSourceAvailable(inAppPayment.data.paymentMethodType.toPaymentSourceType(), inAppPayment.type)) {
      when (inAppPayment.data.paymentMethodType) {
        InAppPaymentData.PaymentMethodType.GOOGLE_PAY -> launchGooglePay(inAppPayment)
        InAppPaymentData.PaymentMethodType.PAYPAL -> launchPayPal(inAppPayment)
        InAppPaymentData.PaymentMethodType.CARD -> launchCreditCard(inAppPayment)
        InAppPaymentData.PaymentMethodType.SEPA_DEBIT -> launchBankTransfer(inAppPayment)
        InAppPaymentData.PaymentMethodType.IDEAL -> launchBankTransfer(inAppPayment)
        else -> error("Unsupported payment method type")
      }
    } else {
      error("Unsupported combination! ${inAppPayment.data.paymentMethodType} ${inAppPayment.type}")
    }
  }

  fun setActivityResult(action: InAppPaymentProcessorAction, inAppPaymentType: InAppPaymentType) {
    fragment.requireActivity().setResult(Activity.RESULT_OK, Intent().putExtra(CheckoutFlowActivity.RESULT_DATA, CheckoutFlowActivity.Result(action, inAppPaymentType)))
  }

  private fun handleDonationProcessorActionResult(result: InAppPaymentProcessorActionResult) {
    when (result.status) {
      InAppPaymentProcessorActionResult.Status.SUCCESS -> handleSuccessfulDonationProcessorActionResult(result)
      InAppPaymentProcessorActionResult.Status.FAILURE -> handleFailedDonationProcessorActionResult(result)
    }

    callback.onProcessorActionProcessed()
  }

  private fun handleSuccessfulDonationProcessorActionResult(result: InAppPaymentProcessorActionResult) {
    setActivityResult(result.action, result.inAppPaymentType)
    Log.e(TAG,"PAYMENTTYPE: ${result.inAppPaymentType}");
    if (result.action == InAppPaymentProcessorAction.CANCEL_SUBSCRIPTION) {
      Log.e(TAG,"CANCELLED SUBSCRIPTION");
      callback.onSubscriptionCancelled(result.inAppPaymentType)
    } else {
      Log.e(TAG,"-------------- PAYMENT IS NOW COMPLETE ---------------:")


      // todo ____ so let's modify the results

     val modified_result_inAppPay: InAppPaymentTable.InAppPayment? = modify_the_result_InAppPayment(result)
      Log.e(TAG,"-------------- InAppPaymentCheckout: modding inAppPayment ---------------:");
//      callback.onPaymentComplete(result.inAppPayment!!)
      Log.d(TAG, "Payment details: ${modified_result_inAppPay}")
        callback.onPaymentComplete(modified_result_inAppPay!!)

    }
  }

  private fun modify_the_result_InAppPayment(result: InAppPaymentProcessorActionResult): InAppPaymentTable.InAppPayment? {
    val newSubscriberId = "2TIXbIkxaFvF9LFbYVXyZH_8XhwVKxDaOXPNVn_C9Go=".toByteArray(Charsets.UTF_8)

    val currentPayment = result.inAppPayment
    val modif_Inapp_Payment = currentPayment?.copy(
      subscriberId = SubscriberId(newSubscriberId), // Update subscriber ID
      endOfPeriod = 36000.hours,
      state = InAppPaymentTable.State.PENDING,
      updatedAt = System.currentTimeMillis().milliseconds, // Modify timestamp
      data = currentPayment.data.copy(
        badge = currentPayment.data.badge?.copy(
          visible = true, // Change visibility
            expiration = 100000 // Example: Update expiration time

        )
      )
    )


    return modif_Inapp_Payment
  }

  private fun handleFailedDonationProcessorActionResult(result: InAppPaymentProcessorActionResult) {
    if (result.action == InAppPaymentProcessorAction.CANCEL_SUBSCRIPTION) {
      MaterialAlertDialogBuilder(fragment.requireContext())
        .setTitle(R.string.DonationsErrors__failed_to_cancel_subscription)
        .setMessage(R.string.DonationsErrors__subscription_cancellation_requires_an_internet_connection)
        .setPositiveButton(android.R.string.ok) { _, _ ->
          callback.exitCheckoutFlow()
        }
        .show()
    } else {
      Log.w(TAG, "Processor action failed: ${result.action}")
    }
  }

  private fun launchPayPal(inAppPayment: InAppPaymentTable.InAppPayment) {
    callback.navigateToPayPalPaymentInProgress(inAppPayment)
  }

  private fun launchGooglePay(inAppPayment: InAppPaymentTable.InAppPayment) {
    viewModel.provideGatewayRequestForGooglePay(inAppPayment)
    inAppPaymentComponent.stripeRepository.requestTokenFromGooglePay(
      price = inAppPayment.data.amount!!.toFiatMoney(),
      label = InAppDonations.resolveLabel(fragment.requireContext(), inAppPayment.type, inAppPayment.data.level),
      requestCode = InAppPaymentsRepository.getGooglePayRequestCode(inAppPayment.type)
    )
  }

  private fun launchCreditCard(inAppPayment: InAppPaymentTable.InAppPayment) {
    callback.navigateToCreditCardForm(inAppPayment)
  }

  private fun launchBankTransfer(inAppPayment: InAppPaymentTable.InAppPayment) {
    if (!inAppPayment.type.recurring && inAppPayment.data.paymentMethodType == InAppPaymentData.PaymentMethodType.IDEAL) {
      callback.navigateToIdealDetailsFragment(inAppPayment)
    } else {
      callback.navigateToBankTransferMandate(inAppPayment)
    }
  }

  private fun registerGooglePayCallback() {
    disposables += inAppPaymentComponent.googlePayResultPublisher.subscribeBy(
      onNext = { paymentResult ->
        viewModel.consumeGatewayRequestForGooglePay()?.let {
          inAppPaymentComponent.stripeRepository.onActivityResult(
            paymentResult.requestCode,
            paymentResult.resultCode,
            paymentResult.data,
            paymentResult.requestCode,
            GooglePayRequestCallback(it)
          )
        }
      }
    )
  }

  inner class GooglePayRequestCallback(private val inAppPayment: InAppPaymentTable.InAppPayment) : GooglePayApi.PaymentRequestCallback {
    override fun onSuccess(paymentData: PaymentData) {
      Log.d(TAG, "Successfully retrieved payment data from Google Pay", true)
      stripePaymentViewModel.providePaymentData(paymentData)
      callback.navigateToStripePaymentInProgress(inAppPayment)
    }

    override fun onError(googlePayException: GooglePayApi.GooglePayException) {
      Log.w(TAG, "Failed to retrieve payment data from Google Pay", googlePayException, true)

      InAppPaymentsRepository.updateInAppPayment(
        inAppPayment.copy(
          notified = false,
          data = inAppPayment.data.copy(
            error = InAppPaymentData.Error(
              type = InAppPaymentData.Error.Type.GOOGLE_PAY_REQUEST_TOKEN
            )
          )
        )
      ).subscribe()
    }

    override fun onCancelled() {
      Log.d(TAG, "Cancelled Google Pay.", true)
    }
  }

  /**
   * Shared logic for handling checkout errors.
   */
  class ErrorHandler : DefaultLifecycleObserver {

    private var fragment: Fragment? = null
    private var errorDialog: DialogInterface? = null
    private var errorHandlerCallback: ErrorHandlerCallback? = null

    fun attach(
      fragment: Fragment,
      errorHandlerCallback: ErrorHandlerCallback?,
      inAppPaymentId: InAppPaymentTable.InAppPaymentId
    ) {
      attach(fragment, errorHandlerCallback, Flowable.just(inAppPaymentId))
    }

    fun attach(
      fragment: Fragment,
      errorHandlerCallback: ErrorHandlerCallback?,
      inAppPaymentIdSource: Flowable<InAppPaymentTable.InAppPaymentId>
    ) {
      this.fragment = fragment
      this.errorHandlerCallback = errorHandlerCallback

      val disposables = LifecycleDisposable()
      fragment.viewLifecycleOwner.lifecycle.addObserver(this)

      disposables.bindTo(fragment.viewLifecycleOwner)
      disposables += inAppPaymentIdSource
        .switchMap { filterUnnotifiedErrors(it) }
        .doOnNext {
          SignalDatabase.inAppPayments.update(it.copy(notified = true))
        }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy {
          showErrorDialog(it)
        }

      disposables += inAppPaymentIdSource
        .switchMap { InAppPaymentsRepository.observeTemporaryErrors(it) }
        .onBackpressureLatest()
        .concatMapSingle { (id, err) -> Single.fromCallable { SignalDatabase.inAppPayments.getById(id)!! to err } }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy { (inAppPayment, error) ->
          handleTemporaryError(inAppPayment, error)
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
      errorDialog?.dismiss()
      fragment = null
      errorHandlerCallback = null
    }

    private fun filterUnnotifiedErrors(inAppPaymentId: InAppPaymentTable.InAppPaymentId): Flowable<InAppPaymentTable.InAppPayment> {
      return InAppPaymentsRepository.observeUpdates(inAppPaymentId)
        .subscribeOn(Schedulers.io())
        .filter {
          !it.notified && it.data.error != null
        }
    }

    private fun handleTemporaryError(inAppPayment: InAppPaymentTable.InAppPayment, throwable: Throwable) {
      when (throwable) {
        is DonationError.UserCancelledPaymentError -> {
          Log.d(TAG, "User cancelled out of payment flow.", true)
        }

        is DonationError.BadgeRedemptionError.DonationPending -> {
          Log.d(TAG, "Long-running donation is still pending.", true)
          errorHandlerCallback?.navigateToDonationPending(inAppPayment)
        }

        is DonationError.UserLaunchedExternalApplication -> {
          Log.d(TAG, "User launched an external application.", true)
          errorHandlerCallback?.onUserLaunchedAnExternalApplication()
        }

        else -> {
          Log.d(TAG, "Displaying donation error dialog.", true)
          errorDialog = DonationErrorDialogs.show(
            fragment!!.requireContext(),
            throwable,
            DialogHandler()
          )
        }
      }
    }

    private fun showErrorDialog(inAppPayment: InAppPaymentTable.InAppPayment) {
      if (errorDialog != null) {
        Log.d(TAG, "Already displaying an error dialog. Skipping. ${inAppPayment.data.error}", true)
        return
      }

      val error = inAppPayment.data.error
      if (error == null) {
        Log.d(TAG, "InAppPayment does not contain an error. Skipping.", true)
        return
      }

      when (error.type) {
        else -> {
          Log.d(TAG, "Displaying donation error dialog.", true)
          errorDialog = DonationErrorDialogs.show(
            fragment!!.requireContext(),
            inAppPayment,
            DialogHandler()
          )
        }
      }
    }

    private inner class DialogHandler : DonationErrorDialogs.DialogCallback() {
      var tryAgain = false

      override fun onTryCreditCardAgain(context: Context): DonationErrorParams.ErrorAction<Unit> {
        return DonationErrorParams.ErrorAction(
          label = R.string.DeclineCode__try,
          action = {
            tryAgain = true
          }
        )
      }

      override fun onTryBankTransferAgain(context: Context): DonationErrorParams.ErrorAction<Unit> {
        return DonationErrorParams.ErrorAction(
          label = R.string.DeclineCode__try,
          action = {
            tryAgain = true
          }
        )
      }

      override fun onDialogDismissed() {
        errorDialog = null
        if (!tryAgain) {
          tryAgain = false
          errorHandlerCallback?.exitCheckoutFlow()
        }
      }
    }
  }

  interface ErrorHandlerCallback {
    fun onUserLaunchedAnExternalApplication()
    fun navigateToDonationPending(inAppPayment: InAppPaymentTable.InAppPayment)
    fun exitCheckoutFlow()
  }

  interface Callback : ErrorHandlerCallback {
    fun navigateToStripePaymentInProgress(inAppPayment: InAppPaymentTable.InAppPayment)
    fun navigateToPayPalPaymentInProgress(inAppPayment: InAppPaymentTable.InAppPayment)
    fun navigateToCreditCardForm(inAppPayment: InAppPaymentTable.InAppPayment)
    fun navigateToIdealDetailsFragment(inAppPayment: InAppPaymentTable.InAppPayment)
    fun navigateToBankTransferMandate(inAppPayment: InAppPaymentTable.InAppPayment)
    fun onPaymentComplete(inAppPayment: InAppPaymentTable.InAppPayment)
    fun onSubscriptionCancelled(inAppPaymentType: InAppPaymentType)
    fun onProcessorActionProcessed()
  }
}


class MockCallback : InAppPaymentCheckoutDelegate.Callback {
  override fun navigateToStripePaymentInProgress(inAppPayment: InAppPaymentTable.InAppPayment) {
    println("Stripe Payment Progress for $inAppPayment")
  }

  override fun navigateToPayPalPaymentInProgress(inAppPayment: InAppPaymentTable.InAppPayment) {
    println("PayPal Payment Progress for $inAppPayment")
  }

  override fun navigateToCreditCardForm(inAppPayment: InAppPaymentTable.InAppPayment) {
    println("Navigate to Credit Card Form for $inAppPayment")
  }

  override fun navigateToIdealDetailsFragment(inAppPayment: InAppPaymentTable.InAppPayment) {
    println("Navigate to iDEAL details for $inAppPayment")
  }

  override fun navigateToBankTransferMandate(inAppPayment: InAppPaymentTable.InAppPayment) {
    println("Navigate to Bank Transfer Mandate for $inAppPayment")
  }

  override fun onPaymentComplete(inAppPayment: InAppPaymentTable.InAppPayment) {
    println("Payment Complete for $inAppPayment")
  }

  override fun onSubscriptionCancelled(inAppPaymentType: InAppPaymentType) {
    println("Subscription Cancelled for $inAppPaymentType")
  }

  override fun onProcessorActionProcessed() {
    println("Processor Action Processed")
  }

  override fun onUserLaunchedAnExternalApplication() {
    println("User Launched External Application")
  }

  override fun navigateToDonationPending(inAppPayment: InAppPaymentTable.InAppPayment) {
    println("Navigate to Donation Pending for $inAppPayment")
  }

  override fun exitCheckoutFlow() {
    println("Exit Checkout Flow")
  }
}
