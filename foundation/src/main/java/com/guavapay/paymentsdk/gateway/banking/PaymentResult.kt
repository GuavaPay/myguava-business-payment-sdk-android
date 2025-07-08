@file:Suppress("unused")

package com.guavapay.paymentsdk.gateway.banking

import android.app.Activity
import androidx.activity.result.ActivityResult
import androidx.core.content.IntentCompat.getSerializableExtra
import com.guavapay.paymentsdk.logging.i
import com.guavapay.paymentsdk.network.services.OrderApi
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.EXTRA_SDK_ERROR_THROWABLE
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.EXTRA_SDK_RESULT_CODE
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.EXTRA_SDK_SUCCESS_PAYMENT_PAYMENT
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.SDK_RESULT_CANCELED
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.SDK_RESULT_COMPLETED
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.SDK_RESULT_DECLINED
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.SDK_RESULT_FAILED
import java.io.Serializable
import java.math.BigDecimal

sealed interface PaymentResult : Serializable {
  data class Completed(val payment: Payment? = null, val order: Order? = null) : PaymentResult
  data class Declined(val payment: Payment? = null, val order: Order? = null) : PaymentResult
  data object Canceled : PaymentResult { private fun readResolve(): Any = Canceled }
  data class Failed(val throwable: Throwable? = null) : PaymentResult

  data class Amount(val amount: BigDecimal, val currency: String) : Serializable
  data class TransactionResult(val code: String?, val message: String?) : Serializable
  data class Reversal(val result: TransactionResult?, val reason: String?) : Serializable
  data class Order(val id: String?, val status: String?, val referenceNumber: String?, val amount: Amount) : Serializable
  data class Payment(val id: String?, val date: String?, val rrn: String?, val authCode: String?, val resultMessage: String?) : Serializable

  companion object {
    internal fun OrderApi.Models.ExtendedAmount.toResult() = Amount(amount = baseUnits, currency = currency.currencyCode)
    internal fun OrderApi.Models.Order.toResult() = Order(id = id, status = status, referenceNumber = referenceNumber, amount = totalAmount.toResult())
    internal fun OrderApi.Models.Payment.toResult() = Payment(id = id, date = date, rrn = rrn, authCode = authCode, resultMessage = result?.message)

    internal fun from(result: ActivityResult): PaymentResult {
      val pr = if (result.resultCode == Activity.RESULT_OK && result.data != null) {
        val intent = result.data ?: error("no result, never must happen")
        val code = intent.getIntExtra(EXTRA_SDK_RESULT_CODE, SDK_RESULT_CANCELED)

        when (code) {
          SDK_RESULT_COMPLETED -> {
            val payment = getSerializableExtra(intent, EXTRA_SDK_SUCCESS_PAYMENT_PAYMENT, Payment::class.java)
            val order = getSerializableExtra(intent, EXTRA_SDK_SUCCESS_PAYMENT_PAYMENT, Order::class.java)
            Completed(payment = payment, order = order)
          }
          SDK_RESULT_DECLINED -> {
            val payment = getSerializableExtra(intent, EXTRA_SDK_SUCCESS_PAYMENT_PAYMENT, Payment::class.java)
            val order = getSerializableExtra(intent, EXTRA_SDK_SUCCESS_PAYMENT_PAYMENT, Order::class.java)
            Declined(payment = payment, order)
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