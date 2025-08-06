@file:Suppress("FunctionName")

package com.guavapay.paymentsdk.gateway.launcher

import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.intl.Locale
import com.guavapay.myguava.business.myguava3ds2.init.ui.GUiCustomization.createWithAppTheme
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

/**
 * The main entry point for interacting with the Payment SDK.
 *
 * This class handles the initialization and launching of the payment flow.
 * An instance of this class is created via the companion object factory `invoke` method
 * or the `rememberPaymentGateway` composable function.
 *
 * Example of use:
 * ```kotlin
 * // In your Composable function
 * val gateway = rememberPaymentGateway(
 *   state = PaymentGatewayPayload(
 *     orderId = "your_order_id",
 *     sessionToken = "your_session_token"
 *   )
 * )
 *
 * Button(onClick = {
 *   val scope = PaymentGatewayCoroutineScope()
 *   scope.launch {
 *     val result = gateway.start()
 *     // Handle the payment result
 *   }
 * }) {
 *   Text("Pay")
 * }
 * ```
 *
 * @see rememberPaymentGateway
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
@Stable open class PaymentGateway internal constructor() : Serializable {
  /**
   * Starts the payment process.
   *
   * This suspend function launches the payment activity and waits for a result.
   * The result is returned as a [PaymentResult].
   *
   * @return A [PaymentResult] indicating the outcome of the payment.
   */
  open suspend fun start(): PaymentResult = suspendCancellableCoroutine { continuation ->
    continuation.resume(PaymentResult.Success())
  }

  companion object {
    /**
     * Creates an instance of [PaymentGateway].
     *
     * @param context The context, which must be a [ComponentActivity].
     * @param state The payload containing all necessary data for the payment.
     * @return An instance of [PaymentGateway].
     */
    operator fun invoke(context: Context, state: PaymentGatewayPayload): PaymentGateway {
      val state0 = state.takeIf { it.locale == null }?.copy(locale = context.locale()) ?: state
      val state = state0.takeIf { it.threedsLooknfeel == null }?.copy(threedsLooknfeel = createWithAppTheme(context as Activity)) ?: state0

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

/**
 * A Composable function that remembers a [PaymentGateway] instance.
 *
 * This function is the recommended way to create a [PaymentGateway] within a Composable UI.
 * It handles the lifecycle of the gateway and the activity result launcher automatically.
 *
 * @param state The payload containing all necessary data for the payment.
 * @return A remembered instance of [PaymentGateway].
 */
@Composable fun rememberPaymentGateway(state: PaymentGatewayPayload): PaymentGateway {
  val activity = LocalActivity.current!!
  val context = LocalContext.current
  val state0 = state.takeIf { it.locale == null }?.copy(locale = Locale.current.platformLocale) ?: state
  val state = state0.takeIf { it.threedsLooknfeel == null }?.copy(threedsLooknfeel = createWithAppTheme(activity)) ?: state0

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

/**
 * Creates a dedicated [CoroutineScope] for payment gateway operations.
 *
 * This scope uses a [SupervisorJob] and [Dispatchers.Main] to ensure that
 * coroutines are launched on the main thread and that failures do not cancel the parent scope.
 *
 * @return A new [CoroutineScope] for the payment gateway.
 */
fun PaymentGatewayCoroutineScope() = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate + CoroutineName("PaymentGatewayCoroutineScope"))
