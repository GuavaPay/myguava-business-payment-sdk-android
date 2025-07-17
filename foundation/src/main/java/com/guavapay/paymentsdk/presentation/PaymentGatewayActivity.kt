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
import androidx.core.content.IntentCompat.getParcelableExtra
import androidx.core.content.IntentCompat.getSerializableExtra
import com.guavapay.paymentsdk.LibraryState.Device
import com.guavapay.paymentsdk.LibraryUnit.Companion.from
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.gateway.banking.PaymentResult.Canceled
import com.guavapay.paymentsdk.gateway.banking.PaymentResult.Completed
import com.guavapay.paymentsdk.gateway.banking.PaymentResult.Declined
import com.guavapay.paymentsdk.gateway.banking.PaymentResult.Failed
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayPayload
import com.guavapay.paymentsdk.logging.i
import com.guavapay.paymentsdk.network.local.localipv4
import com.guavapay.paymentsdk.platform.function.ℓ
import com.guavapay.myguava.business.myguava3ds2.init.ui.GUiCustomization

internal class PaymentGatewayActivity : ComponentActivity() {
  init {
    i("SDK activity initialization started")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState).also { i("SDK activity creating started with intent: $intent, instance: $savedInstanceState") }
    ensureSdkState()
    enableSecureFlags()
    enableEdgeToEdge()
    setContent { PaymentGatewayBottomSheet(::finishWithResult) }
  }

  private fun ensureSdkState() {
    from(this).apply {
      state.payload = ensurePaymentState()
      state.device = ensureDeviceData()
    }
  }

  override fun onResume() {
    super.onResume().also { i("SDK activity resuming") }
    enableSecureFlags()
  }

  override fun onPause() {
    super.onPause().also { i("SDK activity pausing") }
    val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
    am.appTasks.forEach { task -> task.setExcludeFromRecents(true) }
  }

  override fun onDestroy() {
    super.onDestroy().also { i("SDK activity destroying") }
  }

  private fun enableSecureFlags() = window.setFlags(FLAG_SECURE, FLAG_SECURE)

  private fun ensureDeviceData() = Device(ip = localipv4())

  private fun ensurePaymentState() =
    getSerializableExtra(intent, EXTRA_SDK_GATEWAY_PAYLOAD, PaymentGatewayPayload::class.java)?.let {
      it.copy(threedsLooknfeel = getParcelableExtra(intent, EXTRA_SDK_GATEWAY_PART_3DS_PAYLOAD, GUiCustomization::class.java))
    } ?: ℓ {
      finishWithError(NullPointerException("Gateway SDK payload was null when it unexpected")); null
    }

  private fun finishWithResult(result: PaymentResult) {
    i("Requested finish SDK activity with result: $result")
    Intent().apply {
      when (result) {
        is Canceled -> putExtra(EXTRA_SDK_RESULT_CODE, SDK_RESULT_CANCELED)
        is Completed -> {
          putExtra(EXTRA_SDK_RESULT_CODE, SDK_RESULT_COMPLETED)
          result.payment?.let { putExtra(EXTRA_SDK_SUCCESS_PAYMENT_PAYMENT, it) }
          result.order?.let { putExtra(EXTRA_SDK_SUCCESS_PAYMENT_ORDER, it) }
        }
        is Declined -> {
          putExtra(EXTRA_SDK_RESULT_CODE, SDK_RESULT_DECLINED)
          result.payment?.let { putExtra(EXTRA_SDK_SUCCESS_PAYMENT_PAYMENT, it) }
          result.order?.let { putExtra(EXTRA_SDK_SUCCESS_PAYMENT_ORDER, it) }
        }
        is Failed -> {
          putExtra(EXTRA_SDK_RESULT_CODE, SDK_RESULT_FAILED)
          putExtra(EXTRA_SDK_ERROR_THROWABLE, result.throwable)
        }
      }
    }.also { setResult(RESULT_OK, it) }
    finishAfterTransition()
  }

  private fun finishWithError(throwable: Throwable? = null) {
    i("Requested finish SDK activity with error: $throwable")
    Intent().apply {
      putExtra(EXTRA_SDK_RESULT_CODE, SDK_RESULT_FAILED)
      putExtra(EXTRA_SDK_ERROR_THROWABLE, throwable)
    }.also { setResult(RESULT_OK, it) }
    finishAfterTransition()
  }

  internal companion object {
    const val EXTRA_SDK_GATEWAY_PAYLOAD = "sdk_gateway_payload"
    const val EXTRA_SDK_GATEWAY_PART_3DS_PAYLOAD = "sdk_gateway_part_3ds_payload"
    const val EXTRA_SDK_RESULT_CODE = "sdk_result_code"
    const val EXTRA_SDK_ERROR_THROWABLE = "sdk_error_throwable"
    const val EXTRA_SDK_SUCCESS_PAYMENT_PAYMENT = "sdk_success_payment_payment"
    const val EXTRA_SDK_SUCCESS_PAYMENT_ORDER = "sdk_success_payment_order"

    const val SDK_RESULT_CANCELED = 0
    const val SDK_RESULT_COMPLETED = 1
    const val SDK_RESULT_DECLINED = 2
    const val SDK_RESULT_FAILED = 3

    const val WINDOW_ANIMATION_DURATION = 400

    val launcher = Launcher() ; class Launcher {
      fun intent(context: Context, state: PaymentGatewayPayload) =
        Intent(context, PaymentGatewayActivity::class.java).apply {
          putExtra(EXTRA_SDK_GATEWAY_PAYLOAD, state.copy(threedsLooknfeel = null) /* exclude parcelable */)
          putExtra(EXTRA_SDK_GATEWAY_PART_3DS_PAYLOAD, state.threedsLooknfeel)
        }
    }
  }
}
