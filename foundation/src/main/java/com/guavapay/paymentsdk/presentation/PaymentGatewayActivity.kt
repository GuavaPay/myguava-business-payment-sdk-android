@file:Suppress("SameParameterValue")

package com.guavapay.paymentsdk.presentation

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.content.IntentCompat.getSerializableExtra
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.gateway.launcher.LocalGatewayState
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayState
import com.guavapay.paymentsdk.platform.function.ℓ

internal class PaymentGatewayActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableSecureFlags()
    enableEdgeToEdge()
    setContent {
      CompositionLocalProvider(LocalGatewayState provides (ensurePaymentState() ?: return@setContent)) {
        PaymentGatewayBottomSheet(::finishWithResult)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    enableSecureFlags()
  }

  override fun onPause() {
    super.onPause()
    val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
    am.appTasks.forEach { task -> task.setExcludeFromRecents(true) }
  }

  private fun enableSecureFlags() = window.setFlags(FLAG_SECURE, FLAG_SECURE)

  private fun ensurePaymentState() =
    getSerializableExtra(intent, EXTRA_PAYMENT_STATE, PaymentGatewayState::class.java) ?: ℓ {
      finishWithError("INVALID_STATE", "Payment state not provided"); null
    }

  private fun finishWithResult(result: PaymentResult) {
    Intent().apply { when (result) {
        is PaymentResult.Canceled -> putExtra(EXTRA_PAYMENT_RESULT_CODE, RESULT_CANCELED)
        is PaymentResult.Completed -> putExtra(EXTRA_PAYMENT_RESULT_CODE, RESULT_COMPLETED)
        is PaymentResult.Failed -> {
          putExtra(EXTRA_PAYMENT_RESULT_CODE, RESULT_FAILED)
          putExtra(EXTRA_PAYMENT_ERROR_CODE, result.error.code)
          putExtra(EXTRA_PAYMENT_ERROR_MESSAGE, result.error.message)
        }
    } }.also { setResult(RESULT_OK, it) }
    finishAfterTransition()
  }

  private fun finishWithError(code: String, message: String) {
    Intent().apply {
      putExtra(EXTRA_PAYMENT_RESULT_CODE, RESULT_FAILED)
      putExtra(EXTRA_PAYMENT_ERROR_CODE, code)
      putExtra(EXTRA_PAYMENT_ERROR_MESSAGE, message)
    }.also { setResult(RESULT_OK, it) }
    finishAfterTransition()
  }

  internal companion object {
    const val EXTRA_PAYMENT_STATE = "payment_state"
    const val EXTRA_PAYMENT_RESULT_CODE = "payment_result_code"
    const val EXTRA_PAYMENT_ERROR_CODE = "payment_error_code"
    const val EXTRA_PAYMENT_ERROR_MESSAGE = "payment_error_message"

    const val RESULT_CANCELED = 0
    const val RESULT_COMPLETED = 1
    const val RESULT_FAILED = 2

    const val WINDOW_ANIMATION_DURATION = 400

    val launcher = Launcher(); class Launcher {
      fun intent(context: Context, state: PaymentGatewayState) =
        Intent(context, PaymentGatewayActivity::class.java).apply {
          putExtra(EXTRA_PAYMENT_STATE, state)
        }
    }
  }
}
