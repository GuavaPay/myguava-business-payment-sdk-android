package com.guavapay.paymentsdk.gateway.vendors.googlepay

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.wallet.AutoResolveHelper.RESULT_ERROR
import com.google.android.gms.wallet.AutoResolveHelper.getStatusFromIntent
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData.getFromIntent
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.Wallet.WalletOptions
import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.gateway.banking.GatewayException.GooglePayException.GooglePayApiException
import com.guavapay.paymentsdk.gateway.banking.GatewayException.GooglePayException.GooglePayNoPaymentDataException
import com.guavapay.paymentsdk.gateway.banking.GatewayException.GooglePayException.GooglePayNotInitializedException
import com.guavapay.paymentsdk.gateway.banking.GatewayException.GooglePayException.GooglePayNotReadyException
import com.guavapay.paymentsdk.gateway.banking.GatewayException.GooglePayException.GooglePayUnknownException
import com.guavapay.paymentsdk.gateway.banking.PaymentEnvironment.Development
import com.guavapay.paymentsdk.gateway.banking.PaymentEnvironment.Production
import com.guavapay.paymentsdk.gateway.banking.PaymentEnvironment.Sandbox
import com.guavapay.paymentsdk.gateway.vendors.googlepay.GPayEnvironment.PRODUCTION
import com.guavapay.paymentsdk.gateway.vendors.googlepay.GPayEnvironment.TEST
import com.guavapay.paymentsdk.gateway.vendors.googlepay.GPayResult.Canceled
import com.guavapay.paymentsdk.gateway.vendors.googlepay.GPayResult.Failed
import com.guavapay.paymentsdk.gateway.vendors.googlepay.GPayResult.Success
import com.guavapay.paymentsdk.network.services.OrderApi.Models.GooglePayContext
import com.guavapay.paymentsdk.network.services.OrderApi.Models.Order
import com.guavapay.paymentsdk.platform.manifest.manifestFields
import com.guavapay.paymentsdk.platform.threading.await
import com.guavapay.paymentsdk.presentation.platform.retrow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

internal class GPayOrchestrator(private val context: Context, private val order: Order, private val gpayctx: GooglePayContext, private val locale: Locale) {
  private val _isProcessing = MutableStateFlow(false)
  val isProcessing: StateFlow<Boolean> get() = _isProcessing

  private val _isReady = MutableStateFlow(false)
  val isReady: StateFlow<Boolean> get() = _isReady

  private val _isContextReady = MutableStateFlow(false)
  val isContextReady: StateFlow<Boolean> get() = _isContextReady

  private var launcher: ActivityResultLauncher<IntentSenderRequest>? = null
  private var pendingContinuation: Continuation<GPayResult>? = null
  private var paymentsClient: PaymentsClient? = null

  fun setLauncher(l: ActivityResultLauncher<IntentSenderRequest>) { launcher = l }

  val buttonPayload get() = buildButtonAllowedPaymentMethodsJson(gpayctx)

  fun onActivityResult(result: ActivityResult) {
    val cont = pendingContinuation ?: return
    pendingContinuation = null
    _isProcessing.value = false

    cont.resume(
      when (result.resultCode) {
        RESULT_OK -> {
          result.data?.let { intent ->
            getFromIntent(intent)?.let { paymentData ->
              val paymentDataJson = paymentData.toJson()
              Success(paymentDataJson)
            }
          } ?: Failed(GooglePayNoPaymentDataException())
        }
        RESULT_ERROR -> {
          val status = getStatusFromIntent(result.data)
          Failed(GooglePayApiException(status?.statusCode, status?.statusMessage.toString()))
        }
        RESULT_CANCELED -> Canceled
        else -> Failed(GooglePayUnknownException("Unknown result code: ${result.resultCode}"))
      }
    )
  }

  suspend fun initialize() {
    try {
      _isContextReady.value = true
      val environment = determineEnvironment()
      paymentsClient = Wallet.getPaymentsClient(context, WalletOptions.Builder().setEnvironment(environment.qualifier).build())
      checkGooglePayReadiness()
    } catch (e: Exception) {
      _isReady.value = false
      _isContextReady.value = false
      retrow(e)
    }
  }

  suspend fun start() = suspendCancellableCoroutine { cont ->
    if (!_isReady.value) {
      cont.resume(Failed(GooglePayNotReadyException("Google Pay is not ready")))
      return@suspendCancellableCoroutine
    }

    val client = paymentsClient
    if (client == null) {
      cont.resume(Failed(GooglePayNotInitializedException("Google Pay client not initialized")))
      return@suspendCancellableCoroutine
    }

    val launcher = launcher
    if (launcher == null) {
      cont.resume(Failed(GooglePayNotInitializedException("Activity launcher not set")))
      return@suspendCancellableCoroutine
    }

    try {
      _isProcessing.value = true
      pendingContinuation = cont

      val paymentDataJson = buildPaymentDataRequestJson(
        context = gpayctx,
        order = order,
        locale = locale
      )

      val request = PaymentDataRequest.fromJson(paymentDataJson)
      val task = client.loadPaymentData(request)

      task.addOnCompleteListener { completedTask ->
        if (completedTask.isSuccessful) {
          return@addOnCompleteListener
        }

        val exception = completedTask.exception
        if (exception is ResolvableApiException) {
          val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
          launcher.launch(intentSenderRequest)
        } else {
          pendingContinuation?.resume(Failed(exception ?: GooglePayUnknownException("Unknown Google Pay error")))
          pendingContinuation = null
          _isProcessing.value = false
        }
      }
    } catch (e: Exception) {
      pendingContinuation = null
      _isProcessing.value = false
      cont.resume(Failed(e))
    }
  }

  private fun determineEnvironment(): GPayEnvironment {
    val circuit = LibraryUnit.from(context).state.payload().environment
    return when (circuit) {
      Production -> PRODUCTION
      Sandbox -> TEST
      Development -> TEST
      null -> {
        val baseUrl = context.manifestFields().baseUrl
        when (baseUrl) {
          "https://api-pgw.myguava.com" -> PRODUCTION
          else -> TEST
        }
      }
    }
  }

  private suspend fun checkGooglePayReadiness() {
    val client = paymentsClient ?: return

    try {
      val isReadyToPayJson = buildIsReadyToPayJson(gpayctx)
      val request = IsReadyToPayRequest.fromJson(isReadyToPayJson)

      val task = client.isReadyToPay(request).await()
      val isReady = task.result ?: false

      _isReady.value = isReady
    } catch (e: Exception) {
      _isReady.value = false
      retrow(e)
    }
  }
}