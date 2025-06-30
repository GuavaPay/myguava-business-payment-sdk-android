@file:Suppress("FunctionName")

package com.guavapay.paymentsdk.gateway.launcher

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.intl.Locale
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity
import com.guavapay.paymentsdk.presentation.platform.locale
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.Serializable
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

private var pendingContinuation: Continuation<PaymentResult>? = null

@Stable open class PaymentGateway internal constructor() : Serializable {
  open suspend fun start(): PaymentResult = suspendCancellableCoroutine { continuation ->
    continuation.resume(PaymentResult.Completed)
  }

  companion object {
    operator fun invoke(context: Context, state: PaymentGatewayPayload): PaymentGateway {
      val state = state.takeIf { it.locale == null }?.copy(locale = context.locale()) ?: state

      require(context is ComponentActivity) { "Context must be a ComponentActivity for registering ActivityResultLauncher" }

      val launcher = context.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        pendingContinuation?.resume(PaymentResult.from(result))
        pendingContinuation = null
      }

      return object : PaymentGateway() {
        override suspend fun start() = suspendCancellableCoroutine { continuation ->
          pendingContinuation = continuation
          launcher.launch(PaymentGatewayActivity.launcher.intent(context, state))
          continuation.invokeOnCancellation { pendingContinuation = null }
        }
      }
    }
  }
}

@Composable fun rememberPaymentGateway(state: PaymentGatewayPayload): PaymentGateway {
  val context = LocalContext.current
  val state = state.takeIf { it.locale == null }?.copy(locale = Locale.current.platformLocale) ?: state

  val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    pendingContinuation?.resume(PaymentResult.from(result))
    pendingContinuation = null
  }

  return remember(state) {
    object : PaymentGateway() {
      override suspend fun start() = suspendCancellableCoroutine { continuation ->
        pendingContinuation = continuation
        launcher.launch(PaymentGatewayActivity.launcher.intent(context, state))
        continuation.invokeOnCancellation { pendingContinuation = null }
      }
    }
  }
}

fun PaymentGatewayCoroutineScope() = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate + CoroutineName("PaymentGatewayCoroutineScope"))