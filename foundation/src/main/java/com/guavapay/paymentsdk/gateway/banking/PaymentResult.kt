@file:Suppress("unused")

package com.guavapay.paymentsdk.gateway.banking

import android.app.Activity
import androidx.activity.result.ActivityResult
import androidx.core.content.IntentCompat.getSerializableExtra
import com.guavapay.paymentsdk.logging.i
import com.guavapay.paymentsdk.network.services.OrderApi
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.EXTRA_SDK_ERROR_THROWABLE
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.EXTRA_SDK_RESULT_CODE
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.EXTRA_SDK_SUCCESS_PAYMENT_ORDER
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.EXTRA_SDK_SUCCESS_PAYMENT_PAYMENT
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.SDK_RESULT_CANCELED
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.SDK_RESULT_COMPLETED
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.SDK_RESULT_UNSUCCESS
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.SDK_RESULT_FAILED
import java.io.Serializable
import java.math.BigDecimal

/**
 * Represents the result of a payment operation handled by the SDK.
 * This is a sealed interface that encapsulates all possible outcomes of a payment flow.
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
sealed interface PaymentResult : Serializable {
  /**
   * Indicates a successful payment.
   *
   * @property payment Details of the successful payment.
   * @property order Details of the associated order.
   */
  data class Success(val payment: Payment? = null, val order: Order? = null) : PaymentResult

  /**
   * Indicates an unsuccessful payment (e.g., declined by the bank).
   *
   * @property payment Details of the unsuccessful payment attempt.
   * @property order Details of the associated order.
   */
  data class Unsuccess(val payment: Payment? = null, val order: Order? = null) : PaymentResult

  /**
   * Indicates that an error occurred during the payment process.
   *
   * @property throwable The exception that occurred.
   */
  data class Error(val throwable: Throwable? = null) : PaymentResult

  /**
   * Indicates that the user cancelled the payment process.
   */
  data object Cancel : PaymentResult { private fun readResolve(): Any = Cancel }

  /**
   * Represents a monetary amount.
   *
   * @property amount The value of the amount.
   * @property currency The currency code (e.g., "USD").
   */
  data class Amount(val amount: BigDecimal, val currency: String) : Serializable

  /**
   * Represents the result of a transaction.
   *
   * @property code The transaction result code.
   * @property message A descriptive message for the transaction result.
   */
  data class TransactionResult(val code: String?, val message: String?) : Serializable

  /**
   * Represents a reversal of a transaction.
   *
   * @property result The result of the reversal transaction.
   * @property reason The reason for the reversal.
   */
  data class Reversal(val result: TransactionResult?, val reason: String?) : Serializable

  /**
   * Represents order details.
   *
   * @property id The unique identifier of the order.
   * @property status The current status of the order.
   * @property referenceNumber The reference number for the order.
   * @property amount The total amount of the order.
   */
  data class Order(val id: String?, val status: String?, val referenceNumber: String?, val amount: Amount) : Serializable

  /**
   * Represents payment details.
   *
   * @property id The unique identifier of the payment.
   * @property date The date of the payment.
   * @property rrn The Retrieval Reference Number.
   * @property authCode The authorization code.
   * @property resultMessage A message describing the result of the payment.
   */
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
            val order = getSerializableExtra(intent, EXTRA_SDK_SUCCESS_PAYMENT_ORDER, Order::class.java)
            Success(payment = payment, order = order)
          }

          SDK_RESULT_UNSUCCESS -> {
            val payment = getSerializableExtra(intent, EXTRA_SDK_SUCCESS_PAYMENT_PAYMENT, Payment::class.java)
            val order = getSerializableExtra(intent, EXTRA_SDK_SUCCESS_PAYMENT_ORDER, Order::class.java)
            Unsuccess(payment = payment, order = order)
          }

          SDK_RESULT_FAILED -> Error(getSerializableExtra(intent, EXTRA_SDK_ERROR_THROWABLE, Throwable::class.java))
          else -> Cancel
        }
      } else {
        Cancel
      }

      return pr.also { i("Finishing SDK activity with result: $it") }
    }
  }
}
