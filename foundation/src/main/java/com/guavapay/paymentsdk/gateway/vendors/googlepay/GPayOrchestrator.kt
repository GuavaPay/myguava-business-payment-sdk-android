package com.guavapay.paymentsdk.gateway.vendors.googlepay

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.Wallet.WalletOptions
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.gateway.banking.PaymentResult.Failed.Error
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayState
import com.guavapay.paymentsdk.platform.threading.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

internal class GPayOrchestrator(context: Context, private val state: PaymentGatewayState, private val locale: Locale) {
  private val gpay = state.instruments.instrument<PaymentMethod.GooglePay>() ?: error("GooglePay instrument missing")
  private val client = Wallet.getPaymentsClient(context, WalletOptions.Builder().setEnvironment(gpay.environment.qualifier).build())

  private val _isProcessing = MutableStateFlow(false)
  val isProcessing: StateFlow<Boolean> get() = _isProcessing

  private val _isReady = MutableStateFlow(false)
  val isReady: StateFlow<Boolean> get() = _isReady

  private var launcher: ActivityResultLauncher<IntentSenderRequest>? = null
  private var pendingContinuation: Continuation<PaymentResult>? = null

  fun setLauncher(l: ActivityResultLauncher<IntentSenderRequest>) {
    launcher = l
  }

  fun onActivityResult(result: ActivityResult) {
    val cont = pendingContinuation ?: return
    pendingContinuation = null
    _isProcessing.value = false

    cont.resume(
      when (result.resultCode) {
        Activity.RESULT_OK -> {
          result.data?.let { intent ->
            PaymentData.getFromIntent(intent)?.let { paymentData ->
              PaymentResult.Completed
            }
          } ?: PaymentResult.Failed(Error("", "can't process googlepay", "no payment data")) // TODO: After integration with common payment method will real error.
        }
        Activity.RESULT_CANCELED -> PaymentResult.Canceled
        AutoResolveHelper.RESULT_ERROR -> {
          val status = AutoResolveHelper.getStatusFromIntent(result.data)
          PaymentResult.Failed(Error("", "can't process googlepay", status?.statusMessage ?: "unknown error")) // TODO: After integration with common payment method will real error.
        }
        else -> PaymentResult.Failed(Error("", "can't process googlepay", "unknown result")) // TODO: After integration with common payment method will real error.
      }
    )
  }

  suspend fun rediness() {
    val ready = client
      .isReadyToPay(IsReadyToPayRequest.fromJson(state.toGoogleIsReadyRequest()))
      .await()
      .result == true
    _isReady.value = ready
  }

  suspend fun start(): PaymentResult = suspendCancellableCoroutine { cont ->
    if (!_isReady.value) {
      cont.resume(PaymentResult.Failed(Error("", "can't process googlepay", "google pay not ready"))) // TODO: After integration with common payment method will real error.
      return@suspendCancellableCoroutine
    }

    val currentLauncher = launcher
    if (currentLauncher == null) {
      cont.resume(PaymentResult.Failed(Error("", "can't process googlepay", "launcher not set"))) // TODO: After integration with common payment method will real error.
      return@suspendCancellableCoroutine
    }

    if (_isProcessing.value) {
      cont.resume(PaymentResult.Failed(Error("", "can't process googlepay", "already processing"))) // TODO: After integration with common payment method will real error.
      return@suspendCancellableCoroutine
    }

    _isProcessing.value = true
    pendingContinuation = cont

    try {
      val countryCode = locale.country
      val paymentDataRequestJson = state.toGooglePayRequest(countryCode)
      val request = PaymentDataRequest.fromJson(paymentDataRequestJson)

      client.loadPaymentData(request)
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            task.result?.let { paymentData ->
              _isProcessing.value = false
              val cont = pendingContinuation
              pendingContinuation = null
              cont?.resume(PaymentResult.Completed)
            }
          } else {
            val exception = task.exception
            if (exception is ResolvableApiException) {
              try {
                val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                currentLauncher.launch(intentSenderRequest)
              } catch (e: Exception) {
                _isProcessing.value = false
                val cont = pendingContinuation
                pendingContinuation = null
                cont?.resume(PaymentResult.Failed(Error("", "can't process googlepay", "failed to start: ${e.message}"))) // TODO: After integration with common payment method will real error.
              }
            } else {
              _isProcessing.value = false
              val cont = pendingContinuation
              pendingContinuation = null
              cont?.resume(PaymentResult.Failed(Error("", "can't process googlepay", "payment failed: ${exception?.message}"))) // TODO: After integration with common payment method will real error.
            }
          }
        }
    } catch (e: Exception) {
      _isProcessing.value = false
      pendingContinuation = null
      cont.resume(PaymentResult.Failed(Error("", "can't process googlepay", "exception: ${e.message}"))) // TODO: After integration with common payment method will real error.
    }

    cont.invokeOnCancellation {
      _isProcessing.value = false
      pendingContinuation = null
    }
  }
}