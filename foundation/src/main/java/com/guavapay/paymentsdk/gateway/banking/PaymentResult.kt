@file:Suppress("unused")

package com.guavapay.paymentsdk.gateway.banking

import android.app.Activity
import androidx.activity.result.ActivityResult
import androidx.core.content.IntentCompat.getSerializableExtra
import com.guavapay.paymentsdk.logging.i
import com.guavapay.paymentsdk.network.services.OrderApi
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.EXTRA_SDK_ERROR_THROWABLE
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.EXTRA_SDK_RESULT_CODE
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.EXTRA_SDK_SUCCESS_PAYMENT
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.SDK_RESULT_CANCELED
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.SDK_RESULT_COMPLETED
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.SDK_RESULT_DECLINED
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.SDK_RESULT_FAILED
import java.io.Serializable

sealed interface PaymentResult : Serializable {
  data class Completed(val payment: Payment? = null) : PaymentResult
  data class Declined(val payment: Payment? = null) : PaymentResult
  data object Canceled : PaymentResult { private fun readResolve(): Any = Canceled }
  data class Failed(val throwable: Throwable? = null) : PaymentResult

  data class ExtendedAmount(val baseUnits: Double, val currency: String, val minorSubunits: Long, val localized: String) : Serializable
  data class TransactionResult(val code: String?, val message: String?) : Serializable
  data class Reversal(val result: TransactionResult?, val reason: String?) : Serializable
  data class Payment(val id: String?, val date: String?, val exchangeRate: Double?, val amount: ExtendedAmount?, val result: TransactionResult?, val rrn: String?, val authCode: String?, val reversal: Reversal?) : Serializable

  companion object {
    private fun OrderApi.Models.ExtendedAmount.toResult() = ExtendedAmount(baseUnits = baseUnits, currency = currency, minorSubunits = minorSubunits, localized = localized)
    private fun OrderApi.Models.TransactionResult.toResult() = TransactionResult(code = code, message = message)
    private fun OrderApi.Models.Reversal.toResult() = Reversal(result = result?.toResult(), reason = reason)
    private fun OrderApi.Models.Payment.toResult() = Payment(id = id, date = date, exchangeRate = exchangeRate, amount = amount?.toResult(), result = result?.toResult(), rrn = rrn, authCode = authCode, reversal = reversal?.toResult())

    internal fun from(result: ActivityResult): PaymentResult {
      val pr = if (result.resultCode == Activity.RESULT_OK && result.data != null) {
        val intent = result.data ?: error("no result, never must happen")
        val code = intent.getIntExtra(EXTRA_SDK_RESULT_CODE, SDK_RESULT_CANCELED)

        when (code) {
          SDK_RESULT_COMPLETED -> {
            val payment = getSerializableExtra(intent, EXTRA_SDK_SUCCESS_PAYMENT, Payment::class.java)
            Completed(payment = payment)
          }
          SDK_RESULT_DECLINED -> {
            val payment = getSerializableExtra(intent, EXTRA_SDK_SUCCESS_PAYMENT, Payment::class.java)
            Declined(payment = payment)
          }
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