package com.guavapay.paymentsdk.gateway.vendors.googlepay

import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks
import com.guavapay.paymentsdk.gateway.banking.PaymentCardType.CREDIT
import com.guavapay.paymentsdk.gateway.banking.PaymentCardType.DEBIT
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json { encodeDefaults = true }

@Serializable internal data class IsReadyToPayRoot(val apiVersion: Int = 2, val apiVersionMinor: Int = 0, val allowedPaymentMethods: List<AllowedPaymentMethod>)
@Serializable internal data class PaymentDataRequestRoot(val apiVersion: Int = 2, val apiVersionMinor: Int = 0, val allowedPaymentMethods: List<AllowedPaymentMethod>, val merchantInfo: MerchantInfo, val transactionInfo: TransactionInfo)
@Serializable internal data class AllowedPaymentMethod(val type: String = "CARD", val parameters: CardParameters, val tokenizationSpecification: TokenizationSpecification)
@Serializable internal data class CardParameters(val allowedAuthMethods: Set<GPayAuthMethods>, val allowedCardNetworks: Set<PaymentCardNetworks>, val allowCreditCards: Boolean, val allowPrepaidCards: Boolean)
@Serializable internal data class TokenizationSpecification(val type: String = "PAYMENT_GATEWAY", val parameters: GatewayParameters)
@Serializable internal data class GatewayParameters(val gateway: String, val gatewayMerchantId: String)
@Serializable internal data class MerchantInfo(val merchantName: String, val merchantId: String? = null)
@Serializable internal data class TransactionInfo(val totalPriceStatus: String = "FINAL", val totalPrice: String, val currencyCode: String, val countryCode: String)

internal fun PaymentGatewayState.toGoogleAllowedMethodsJson() = json.encodeToString(instruments.instrument<PaymentMethod.GooglePay>()?.describeCanonical() ?: error("PaymentMethod.GooglePay is missing"))

internal fun PaymentGatewayState.toGooglePayRequest(countryCode: String, apiVersion: Int = 2, apiVersionMinor: Int = 0) = json.encodeToString(
  PaymentDataRequestRoot(
    apiVersion = apiVersion,
    apiVersionMinor = apiVersionMinor,
    allowedPaymentMethods = instruments.instrument<PaymentMethod.GooglePay>()?.describeCanonical() ?: error("PaymentMethod.GooglePay is missing"),
    merchantInfo = MerchantInfo(
      merchantName = merchant,
      merchantId = merchant
    ),
    transactionInfo = TransactionInfo(
      totalPrice = "%.2f".format(((amount.value * 100.toBigDecimal()).longValueExact()) / 100.0),
      currencyCode = amount.currency.currencyCode,
      countryCode = countryCode
    )
  )
)

internal fun PaymentGatewayState.toGoogleIsReadyRequest(apiVersion: Int = 2, apiVersionMinor: Int = 0) = json.encodeToString(
  IsReadyToPayRoot(
    apiVersion = apiVersion,
    apiVersionMinor = apiVersionMinor,
    allowedPaymentMethods = instruments.instrument<PaymentMethod.GooglePay>()?.describeCanonical() ?: error("PaymentMethod.GooglePay is missing")
  )
)

internal fun PaymentMethod.GooglePay.describeCanonical() =
  listOf(
    AllowedPaymentMethod(
      parameters = CardParameters(allowedAuthMethods = authmethods, allowedCardNetworks = networks, allowCreditCards = CREDIT in cardtypes, allowPrepaidCards = DEBIT in cardtypes),
      tokenizationSpecification = TokenizationSpecification(
        parameters = GatewayParameters(gateway = merchant, gatewayMerchantId = merchant)
      )
    )
  )