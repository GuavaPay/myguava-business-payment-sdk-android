package com.guavapay.paymentsdk.network.services

import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks
import com.guavapay.paymentsdk.gateway.banking.PaymentCardType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApi {
  @POST("order")
  suspend fun createOrder(
    @Header("Accept-Language") acceptLanguage: String? = null,
    @Header("Correlation-ID") correlationId: String? = null,
    @Body request: Models.CreateOrderRequest
  ): Response<Models.CreateOrderResponse>

  @PUT("order/{orderId}/recurrence")
  suspend fun updateOrderRecurrence(
    @Path("orderId") orderId: String,
    @Body request: Models.UpdateRecurrenceRequest
  ): Response<Models.UpdateRecurrenceResponse>

  @POST("order/{orderId}/recurrence/close")
  suspend fun closeRecurrenceOrder(
    @Path("orderId") orderId: String
  ): Response<Unit>

  @GET("order/{orderId}")
  suspend fun getOrder(
    @Path("orderId") orderId: String,
    @Header("Accept-Language") acceptLanguage: String? = null,
    @Query("merchant-included") merchantIncluded: Boolean? = null,
    @Query("transactions-included") transactionsIncluded: Boolean? = null,
    @Query("payment-requirements-included") paymentRequirementsIncluded: Boolean? = null
  ): Response<Models.GetOrderResponse>

  @GET("order/by-correlation-id/{correlationId}")
  suspend fun getOrderByCorrelationId(
    @Path("correlationId") correlationId: String,
    @Header("Accept-Language") acceptLanguage: String? = null,
    @Query("transactions-included") transactionsIncluded: Boolean? = null
  ): Response<Models.GetOrderResponse>

  @POST("cryptocurrency/exchange/supported")
  suspend fun determineSupportedCryptocurrencyList(
    @Body request: Models.SupportedCryptocurrencyRequest
  ): Response<Models.SupportedCryptocurrencyResponse>

  @POST("cryptocurrency/exchange/rate")
  suspend fun calcExchangeRate(
    @Body request: Models.ExchangeRateRequest
  ): Response<Models.ExchangeRateResponse>

  @POST("card-range/resolve")
  suspend fun getCardRangeData(@Body request: Models.CardRangeRequest): Response<Models.CardRangeResponse>

  @PUT("order/{orderId}/payment")
  suspend fun createPayment(
    @Path("orderId") orderId: String,
    @Body request: Models.CreatePaymentRequest
  ): Response<Models.CreatePaymentResponse>

  @POST("order/{orderId}/payment/execute")
  suspend fun executePayment(
    @Path("orderId") orderId: String,
    @Header("Correlation-ID") correlationId: String? = null,
    @Body request: Models.ExecutePaymentRequest
  ): Response<Models.ExecutePaymentResponse>

  @POST("order/{orderId}/payment/continue")
  suspend fun continuePayment(
    @Path("orderId") orderId: String,
    @Body request: Models.ContinuePaymentRequest
  ): Response<Models.ContinuePaymentResponse>

  @POST("order/{orderId}/subsequent-payment")
  suspend fun subsequentPayment(
    @Path("orderId") orderId: String,
    @Header("Correlation-ID") correlationId: String? = null,
    @Body request: Models.SubsequentPaymentRequest
  ): Response<Models.SubsequentPaymentResponse>

  @POST("order/{orderId}/cancel")
  suspend fun cancelOrder(
    @Path("orderId") orderId: String
  ): Response<Unit>

  @POST("card/resolve")
  suspend fun resolveCardPaymentEligibility(
    @Header("Order-ID") orderId: String? = null,
    @Header("Is-SuperAdmin") isSuperAdmin: Boolean? = null,
    @Body request: Models.CardResolveRequest
  ): Response<Models.CardResolveResponse>

  @GET("googlepay/context")
  suspend fun getGooglePayContext(): Response<Models.GooglePayContextResponse>

  object Models {
    @Serializable
    data class CreateOrderRequest(
      @SerialName("referenceNumber") val referenceNumber: String,
      @SerialName("totalAmount") val totalAmount: Amount,
      @SerialName("callbackUrl") val callbackUrl: String,
      @SerialName("payerId") val payerId: String? = null,
      @SerialName("terminalId") val terminalId: String? = null,
      @SerialName("purpose") val purpose: String? = null,
      @SerialName("redirectUrl") val redirectUrl: String? = null,
      @SerialName("merchantUrl") val merchantUrl: String? = null,
      @SerialName("shippingAddress") val shippingAddress: String? = null,
      @SerialName("description") val description: OrderDescription? = null,
      @SerialName("payer") val payer: Payer? = null,
      @SerialName("expirationOptions") val expirationOptions: ExpirationOptions? = null,
      @SerialName("recurrence") val recurrence: Recurrence? = null,
      @SerialName("shortPaymentPageUrlIsNeeded") val shortPaymentPageUrlIsNeeded: Boolean? = null
    )

    @Serializable
    data class CreateOrderResponse(
      @SerialName("order") val order: Order
    )

    @Serializable
    data class UpdateRecurrenceRequest(
      @SerialName("description") val description: String? = null,
      @SerialName("schedule") val schedule: String? = null,
      @SerialName("startDate") val startDate: String? = null,
      @SerialName("endDate") val endDate: String? = null
    )

    @Serializable
    data class UpdateRecurrenceResponse(
      @SerialName("order") val order: Order
    )

    @Serializable
    data class GetOrderResponse(
      @SerialName("order") val order: Order,
      @SerialName("merchant") val merchant: Merchant? = null,
      @SerialName("payment") val payment: Payment? = null,
      @SerialName("refunds") val refunds: List<Refund>? = null,
      @SerialName("paymentRequirements") val paymentRequirements: PaymentRequirements? = null
    )

    @Serializable
    data class SupportedCryptocurrencyRequest(
      @SerialName("orderCurrency") val orderCurrency: String,
      @SerialName("paymentCurrencies") val paymentCurrencies: List<String>
    )

    @Serializable
    data class SupportedCryptocurrencyResponse(
      @SerialName("paymentCurrencies") val paymentCurrencies: List<String>
    )

    @Serializable
    data class ExchangeRateRequest(
      @SerialName("orderAmount") val orderAmount: Amount? = null,
      @SerialName("paymentCurrency") val paymentCurrency: String
    )

    @Serializable
    data class ExchangeRateResponse(
      @SerialName("from") val from: String? = null,
      @SerialName("to") val to: String? = null,
      @SerialName("rate") val rate: Double? = null,
      @SerialName("orderAmount") val orderAmount: ExtendedAmount? = null,
      @SerialName("paymentAmount") val paymentAmount: ExtendedAmount? = null,
      @SerialName("token") val token: String? = null
    )

    @Serializable
    data class CardRangeRequest(
      @SerialName("rangeIncludes") val rangeIncludes: String? = null
    )

    @Serializable
    data class CardRangeResponse(
      @SerialName("cardScheme") val cardScheme: PaymentCardNetworks? = null,
      @SerialName("product") val product: CardProduct? = null
    )

    @Serializable
    data class CardResolveRequest(
      @SerialName("panIncludes") val panIncludes: String? = null
    )

    @Serializable
    data class CardResolveResponse(
      @SerialName("cardScheme") val cardScheme: PaymentCardNetworks,
      @SerialName("paymentLockReason") val paymentLockReason: String? = null
    )

    @Serializable
    data class CreatePaymentRequest(
      @SerialName("paymentMethod") val paymentMethod: PaymentMethod? = null,
      @SerialName("deviceData") val deviceData: DeviceData? = null,
      @SerialName("payer") val payer: Payer? = null
    )

    @Serializable
    data class CreatePaymentResponse(
      @SerialName("requirements") val requirements: PaymentRequirements? = null
    )

    @Serializable
    data class ExecutePaymentRequest(
      @SerialName("paymentMethod") val paymentMethod: PaymentMethod? = null,
      @SerialName("deviceData") val deviceData: DeviceData? = null,
      @SerialName("bindingCreationIsNeeded") val bindingCreationIsNeeded: Boolean? = null,
      @SerialName("exchange") val exchange: Exchange? = null,
      @SerialName("payer") val payer: Payer? = null,
      @SerialName("challengeWindowSize") val challengeWindowSize: String? = null,
      @SerialName("priorityRedirectUrl") val priorityRedirectUrl: String? = null
    )

    @Serializable
    data class ExecutePaymentResponse(
      @SerialName("redirectUrl") val redirectUrl: String? = null,
      @SerialName("requirements") val requirements: PaymentRequirements? = null
    )

    @Serializable
    data class ContinuePaymentRequest(
      @SerialName("payPalOrderApproveEvent") val payPalOrderApproveEvent: String? = null,
      @SerialName("threedsSdkData") val threedsSdkData: ThreedsSDKData? = null
    )

    @Serializable
    data class ContinuePaymentResponse(
      @SerialName("redirectUrl") val redirectUrl: String? = null,
      @SerialName("requirements") val requirements: PaymentRequirements? = null
    )

    @Serializable
    data class SubsequentPaymentRequest(
      @SerialName("description") val description: String? = null,
      @SerialName("referenceNumber") val referenceNumber: String? = null,
      @SerialName("amount") val amount: Amount? = null
    )

    @Serializable
    data class SubsequentPaymentResponse(
      @SerialName("result") val result: TransactionResult
    )

    @Serializable
    data class GooglePayContextResponse(
      @SerialName("context") val context: GooglePayContext
    )

    @Serializable
    data class Amount(
      @SerialName("baseUnits") val baseUnits: Double,
      @SerialName("currency") val currency: String
    )

    @Serializable
    data class ExtendedAmount(
      @SerialName("baseUnits") val baseUnits: Double,
      @SerialName("currency") val currency: String,
      @SerialName("minorSubunits") val minorSubunits: Long,
      @SerialName("localized") val localized: String
    )

    @Serializable
    data class Order(
      @SerialName("id") val id: String,
      @SerialName("status") val status: String,
      @SerialName("serviceChannel") val serviceChannel: String,
      @SerialName("totalAmount") val totalAmount: ExtendedAmount,
      @SerialName("expirationDate") val expirationDate: String,
      @SerialName("sessionToken") val sessionToken: String,
      @SerialName("availablePaymentMethods") val availablePaymentMethods: List<String>,
      @SerialName("availableCardSchemes") val availableCardSchemes: List<String>,
      @SerialName("availableCardProductCategories") val availableCardProductCategories: List<String>,
      @SerialName("redirectUrl") val redirectUrl: String,
      @SerialName("purpose") val purpose: String,
      @SerialName("referenceNumber") val referenceNumber: String? = null,
      @SerialName("paymentPageUrl") val paymentPageUrl: String? = null,
      @SerialName("shortPaymentPageUrl") val shortPaymentPageUrl: String? = null,
      @SerialName("refundedAmount") val refundedAmount: ExtendedAmount? = null,
      @SerialName("recurrence") val recurrence: Recurrence? = null,
      @SerialName("description") val description: OrderDescription? = null,
      @SerialName("payer") val payer: Payer? = null
    )

    @Serializable
    data class OrderDescription(
      @SerialName("textDescription") val textDescription: String? = null,
      @SerialName("items") val items: List<OrderItem>? = null
    )

    @Serializable
    data class OrderItem(
      @SerialName("barcodeNumber") val barcodeNumber: String? = null,
      @SerialName("vendorCode") val vendorCode: String? = null,
      @SerialName("productProvider") val productProvider: String? = null,
      @SerialName("name") val name: String? = null,
      @SerialName("count") val count: Int? = null,
      @SerialName("unitPrice") val unitPrice: ExtendedAmount? = null,
      @SerialName("totalCost") val totalCost: ExtendedAmount? = null,
      @SerialName("discountAmount") val discountAmount: ExtendedAmount? = null,
      @SerialName("taxAmount") val taxAmount: ExtendedAmount? = null
    )

    @Serializable
    data class ExpirationOptions(
      @SerialName("lifespanTimeoutSeconds") val lifespanTimeoutSeconds: Int? = null,
      @SerialName("expirationDate") val expirationDate: String? = null
    )

    @Serializable
    data class Recurrence(
      @SerialName("execution") val execution: String? = null,
      @SerialName("initialOperation") val initialOperation: String? = null,
      @SerialName("description") val description: String? = null,
      @SerialName("schedule") val schedule: String? = null,
      @SerialName("startDate") val startDate: String? = null,
      @SerialName("endDate") val endDate: String? = null,
      @SerialName("amount") val amount: ExtendedAmount? = null,
      @SerialName("maxAmount") val maxAmount: ExtendedAmount? = null
    )

    @Serializable
    data class Payer(
      @SerialName("id") val id: String? = null,
      @SerialName("firstName") val firstName: String? = null,
      @SerialName("lastName") val lastName: String? = null,
      @SerialName("dateOfBirth") val dateOfBirth: String? = null,
      @SerialName("contactEmail") val contactEmail: String? = null,
      @SerialName("contactPhone") val contactPhone: Phone? = null,
      @SerialName("address") val address: Address? = null,
      @SerialName("inputMode") val inputMode: String? = null
    )

    @Serializable
    data class Phone(
      @SerialName("countryCode") val countryCode: String? = null,
      @SerialName("nationalNumber") val nationalNumber: String? = null,
      @SerialName("country") val country: String? = null,
      @SerialName("fullNumber") val fullNumber: String? = null
    )

    @Serializable
    data class Address(
      @SerialName("country") val country: String? = null,
      @SerialName("city") val city: String? = null,
      @SerialName("state") val state: String? = null,
      @SerialName("zipCode") val zipCode: String? = null,
      @SerialName("addressLine1") val addressLine1: String? = null,
      @SerialName("addressLine2") val addressLine2: String? = null
    )

    @Serializable
    data class PaymentMethod(
      @SerialName("type") val type: String,
      @SerialName("pan") val pan: String? = null,
      @SerialName("cvv2") val cvv2: String? = null,
      @SerialName("expiryDate") val expiryDate: String? = null,
      @SerialName("cardholderName") val cardholderName: String? = null,
      @SerialName("bindingId") val bindingId: String? = null,
      @SerialName("payment") val payment: GooglePaymentData? = null,
      @SerialName("paymentData") val paymentData: GooglePaymentData? = null
    )

    @Serializable
    data class GooglePaymentData(
      @SerialName("paymentMethodData") val paymentMethodData: Map<String, String>? = null
    )

    @Serializable
    data class DeviceData(
      @SerialName("browserData") val browserData: BrowserData? = null,
      @SerialName("ip") val ip: String? = null,
      @SerialName("threedsSdkData") val threedsSdkData: ThreedsSDKData? = null
    )

    @Serializable
    data class BrowserData(
      @SerialName("acceptHeader") val acceptHeader: String? = null,
      @SerialName("userAgent") val userAgent: String? = null,
      @SerialName("javaScriptEnabled") val javaScriptEnabled: Boolean? = null,
      @SerialName("language") val language: String? = null,
      @SerialName("screenHeight") val screenHeight: Int? = null,
      @SerialName("screenWidth") val screenWidth: Int? = null,
      @SerialName("timeZone") val timeZone: Double? = null,
      @SerialName("timeZoneOffset") val timeZoneOffset: Double? = null,
      @SerialName("javaEnabled") val javaEnabled: Boolean? = null,
      @SerialName("screenColorDepth") val screenColorDepth: Int? = null,
      @SerialName("sessionId") val sessionId: String? = null
    )

    @Serializable
    data class ThreedsSDKData(
      @SerialName("name") val name: String,
      @SerialName("version") val version: String,
      @SerialName("packedAuthenticationData") val packedAuthenticationData: String? = null
    )

    @Serializable
    data class Exchange(
      @SerialName("amount") val amount: Amount,
      @SerialName("token") val token: String
    )

    @Serializable
    data class PaymentRequirements(
      @SerialName("threedsMethod") val threedsMethod: ThreeDSMethodRequirements? = null,
      @SerialName("threedsChallenge") val threedsChallenge: ThreeDSChallengeRequirements? = null,
      @SerialName("threedsSdkCreateTransaction") val threedsSdkCreateTransaction: ThreeDSSDKCreateTransactionRequirements? = null,
      @SerialName("payerAuthorization") val payerAuthorization: OpenBankingAuthRequirements? = null,
      @SerialName("cryptocurrencyTransfer") val cryptocurrencyTransfer: CryptocurrencyRequirements? = null,
      @SerialName("payPalOrderApprove") val payPalOrderApprove: PayPalRequirements? = null,
      @SerialName("finishPageRedirect") val finishPageRedirect: FinishPageRedirectRequirements? = null
    )

    @Serializable
    data class ThreeDSMethodRequirements(
      @SerialName("data") val data: String? = null,
      @SerialName("url") val url: String? = null
    )

    @Serializable
    data class ThreeDSChallengeRequirements(
      @SerialName("data") val data: String? = null,
      @SerialName("url") val url: String? = null,
      @SerialName("packedSdkChallengeParameters") val packedSdkChallengeParameters: String? = null
    )

    @Serializable
    data class ThreeDSSDKCreateTransactionRequirements(
      @SerialName("messageVersion") val messageVersion: String? = null,
      @SerialName("directoryServerID") val directoryServerID: String? = null
    )

    @Serializable
    data class OpenBankingAuthRequirements(
      @SerialName("authorizationUrl") val authorizationUrl: String? = null,
      @SerialName("qrCodeData") val qrCodeData: String? = null,
      @SerialName("expirationDate") val expirationDate: String? = null
    )

    @Serializable
    data class CryptocurrencyRequirements(
      @SerialName("walletAddress") val walletAddress: String? = null,
      @SerialName("expirationDate") val expirationDate: String? = null,
      @SerialName("networkName") val networkName: String? = null,
      @SerialName("detectedAmount") val detectedAmount: ExtendedAmount? = null
    )

    @Serializable
    data class PayPalRequirements(
      @SerialName("actionUrl") val actionUrl: String? = null,
      @SerialName("orderId") val orderId: String? = null
    )

    @Serializable
    data class FinishPageRedirectRequirements(
      @SerialName("url") val url: String? = null,
      @SerialName("message") val message: String? = null
    )

    @Serializable
    data class CardProduct(
      @SerialName("id") val id: String,
      @SerialName("brand") val brand: String,
      @SerialName("category") val category: PaymentCardType
    )

    @Serializable
    data class GooglePayContext(
      @SerialName("googleId") val googleId: String? = null,
      @SerialName("displayName") val displayName: String? = null,
      @SerialName("gateway") val gateway: String? = null,
      @SerialName("gatewayMerchantId") val gatewayMerchantId: String? = null,
      @SerialName("allowedCardSchemes") val allowedCardSchemes: List<String>? = null
    )

    @Serializable
    data class Merchant(
      @SerialName("name") val name: String? = null
    )

    @Serializable
    data class Payment(
      @SerialName("id") val id: String? = null,
      @SerialName("date") val date: String? = null,
      @SerialName("exchangeRate") val exchangeRate: Double? = null,
      @SerialName("amount") val amount: ExtendedAmount? = null,
      @SerialName("referenceNumber") val referenceNumber: String? = null,
      @SerialName("result") val result: TransactionResult? = null,
      @SerialName("rrn") val rrn: String? = null,
      @SerialName("authCode") val authCode: String? = null,
      @SerialName("paymentMethod") val paymentMethod: PaymentMethod? = null,
      @SerialName("reversal") val reversal: Reversal? = null
    )

    @Serializable
    data class Refund(
      @SerialName("id") val id: String? = null,
      @SerialName("date") val date: String? = null,
      @SerialName("originalId") val originalId: String? = null,
      @SerialName("result") val result: TransactionResult? = null,
      @SerialName("rrn") val rrn: String? = null,
      @SerialName("authCode") val authCode: String? = null,
      @SerialName("reason") val reason: String? = null,
      @SerialName("amount") val amount: ExtendedAmount,
      @SerialName("items") val items: List<OrderItem>? = null
    )

    @Serializable
    data class Reversal(
      @SerialName("result") val result: TransactionResult? = null,
      @SerialName("reason") val reason: String? = null
    )

    @Serializable
    data class TransactionResult(
      @SerialName("code") val code: String,
      @SerialName("message") val message: String
    )
  }
}