@file:Suppress("unused")

package com.guavapay.paymentsdk.gateway.banking

import android.app.Activity
import androidx.activity.result.ActivityResult
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.EXTRA_PAYMENT_ERROR_CODE
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.EXTRA_PAYMENT_ERROR_MESSAGE
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.RESULT_COMPLETED
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.RESULT_FAILED
import com.guavapay.paymentsdk.gateway.banking.PaymentResult.Failed.Error
import java.io.Serializable

sealed interface PaymentResult : Serializable {
  data object Completed : PaymentResult {
    private fun readResolve(): Any = Completed
  }

  data class Failed(val error: Error) : PaymentResult {
    data class Error(val code: String, val message: String, val details: String? = null) : Serializable
  }

  data object Canceled : PaymentResult {
    private fun readResolve(): Any = Canceled
  }

  companion object {
    internal fun from(result: ActivityResult): PaymentResult {
      return if (result.resultCode == Activity.RESULT_OK && result.data != null) {
        val intent = result.data ?: error("no result, never must happen")
        val code = intent.getIntExtra(PaymentGatewayActivity.EXTRA_PAYMENT_RESULT_CODE, PaymentGatewayActivity.RESULT_CANCELED)

        when (code) {
          RESULT_COMPLETED -> Completed
          RESULT_FAILED -> {
            val errorCode = intent.getStringExtra(EXTRA_PAYMENT_ERROR_CODE) ?: "UNKNOWN_ERROR"
            val errorMessage = intent.getStringExtra(EXTRA_PAYMENT_ERROR_MESSAGE) ?: "Unknown error"
            Failed(Error(errorCode, errorMessage))
          }
          else -> Canceled
        }
      } else {
        Canceled
      }
    }
  }
}