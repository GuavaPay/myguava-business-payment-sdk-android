@file:Suppress("unused")

package com.guavapay.paymentsdk.gateway.banking

import android.app.Activity
import androidx.activity.result.ActivityResult
import androidx.core.content.IntentCompat.getSerializableExtra
import com.guavapay.paymentsdk.logging.i
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.EXTRA_SDK_ERROR_THROWABLE
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.EXTRA_SDK_RESULT_CODE
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.SDK_RESULT_CANCELED
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.SDK_RESULT_COMPLETED
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.SDK_RESULT_FAILED
import java.io.Serializable

sealed interface PaymentResult : Serializable {
  data object Completed : PaymentResult { private fun readResolve(): Any = Completed }
  data object Canceled : PaymentResult { private fun readResolve(): Any = Canceled }
  data class Failed(val throwable: Throwable? = null) : PaymentResult

  companion object {
    internal fun from(result: ActivityResult): PaymentResult {
      val pr = if (result.resultCode == Activity.RESULT_OK && result.data != null) {
        val intent = result.data ?: error("no result, never must happen")
        val code = intent.getIntExtra(EXTRA_SDK_RESULT_CODE, SDK_RESULT_CANCELED)

        when (code) {
          SDK_RESULT_COMPLETED -> Completed
          SDK_RESULT_FAILED -> Failed(getSerializableExtra(intent, EXTRA_SDK_ERROR_THROWABLE, Throwable::class.java))
          else -> Canceled
        }
      } else {
        Canceled
      }

      return pr.also { i("Finishing SDK activity with result: $it") }
    }
  }
}