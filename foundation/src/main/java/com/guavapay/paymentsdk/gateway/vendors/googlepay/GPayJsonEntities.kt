package com.guavapay.paymentsdk.gateway.vendors.googlepay

import com.guavapay.paymentsdk.gateway.banking.PaymentCardScheme
import com.guavapay.paymentsdk.gateway.banking.PaymentCardScheme.AMERICAN_EXPRESS
import com.guavapay.paymentsdk.gateway.banking.PaymentCardScheme.MASTERCARD
import com.guavapay.paymentsdk.gateway.banking.PaymentCardScheme.UNIONPAY
import com.guavapay.paymentsdk.gateway.banking.PaymentCardScheme.VISA
import com.guavapay.paymentsdk.network.services.OrderApi.Models.GooglePayContext
import com.guavapay.paymentsdk.network.services.OrderApi.Models.Order
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Locale

// file:dtos generated by dto from json generator.

private val json = Json { encodeDefaults = true ; ignoreUnknownKeys = true }

@Serializable internal data class IsReadyToPayRoot(val apiVersion: Int = 2, val apiVersionMinor: Int = 0, val allowedPaymentMethods: List<AllowedPaymentMethod>)
@Serializable internal data class PaymentDataRequestRoot(val apiVersion: Int = 2, val apiVersionMinor: Int = 0, val allowedPaymentMethods: List<AllowedPaymentMethod>, val transactionInfo: TransactionInfo, val merchantInfo: MerchantInfo)
@Serializable internal data class AllowedPaymentMethod(val type: String, val parameters: CardParameters, val tokenizationSpecification: TokenizationSpecification? = null)
@Serializable internal data class CardParameters(val allowedAuthMethods: List<String>, val allowedCardNetworks: List<String>)
@Serializable internal data class TokenizationSpecification(val type: String, val parameters: TokenizationParameters)
@Serializable internal data class TokenizationParameters(val gateway: String, val gatewayMerchantId: String)
@Serializable internal data class TransactionInfo(val totalPriceStatus: String, val totalPrice: String, val currencyCode: String, val countryCode: String)
@Serializable internal data class MerchantInfo(val merchantId: String, val merchantName: String)

internal val PaymentCardScheme.gpayname get() = when (this) {
  VISA -> "VISA"
  MASTERCARD -> "MASTERCARD"
  AMERICAN_EXPRESS -> "AMEX"
  UNIONPAY -> "UNIONPAY"
  else -> this.name
}

internal fun buildIsReadyToPayJson(context: GooglePayContext) =
  json.encodeToString(
    IsReadyToPayRoot(
      allowedPaymentMethods = listOf(
        AllowedPaymentMethod(
          type = "CARD",
          parameters = CardParameters(
            allowedAuthMethods = context.allowedAuthMethods,
            allowedCardNetworks = context.allowedCardSchemes
          )
        )
      )
    )
  )

internal fun buildPaymentDataRequestJson(context: GooglePayContext, order: Order, locale: Locale): String {
  val allowedMethods = listOf(
    AllowedPaymentMethod(
      type = "CARD",
      parameters = CardParameters(
        allowedAuthMethods = context.allowedAuthMethods,
        allowedCardNetworks = context.allowedCardSchemes
      ),
      tokenizationSpecification = TokenizationSpecification(
        type = "PAYMENT_GATEWAY",
        parameters = TokenizationParameters(
          gateway = context.gateway.toString(),
          gatewayMerchantId = context.gatewayMerchantId.toString()
        )
      )
    )
  )

  val transactionInfo = TransactionInfo(
    totalPriceStatus = "FINAL",
    totalPrice = order.totalAmount.baseUnits.toString(),
    currencyCode = order.totalAmount.currency.currencyCode,
    countryCode = locale.country.takeIf(String::isNotBlank) ?: "US" // Never must happen.
  )

  val merchantInfo = MerchantInfo(merchantId = context.googleId.toString(), merchantName = context.displayName.toString())
  return json.encodeToString(PaymentDataRequestRoot(allowedPaymentMethods = allowedMethods, transactionInfo = transactionInfo, merchantInfo = merchantInfo))
}

internal fun buildButtonAllowedPaymentMethodsJson(context: GooglePayContext) =
  json.encodeToString(listOf(
    AllowedPaymentMethod(
      type = "CARD",
      parameters = CardParameters(
        allowedAuthMethods = context.allowedAuthMethods,
        allowedCardNetworks = context.allowedCardSchemes
      ),
      tokenizationSpecification = TokenizationSpecification(
       type = "PAYMENT_GATEWAY",
       parameters = TokenizationParameters(
          gateway = context.gateway.toString(),
          gatewayMerchantId = context.gatewayMerchantId.toString()
       )
      )
    )
  ))