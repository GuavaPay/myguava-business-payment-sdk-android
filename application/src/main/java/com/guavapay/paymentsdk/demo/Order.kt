package com.guavapay.paymentsdk.demo

import com.guavapay.paymentsdk.gateway.banking.PaymentEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import kotlin.random.Random

private val client = OkHttpClient().newBuilder().callTimeout(30L, TimeUnit.SECONDS).connectTimeout(30L, TimeUnit.SECONDS).readTimeout(30L, TimeUnit.SECONDS).writeTimeout(30L, TimeUnit.SECONDS).addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }).build()
private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

suspend fun createOrder(baseUrl: String, token: String, amount: Double, currency: String, phoneNumber: String, email: String): OrderResponse {
  return withContext(Dispatchers.IO) {
    val trimmedPhone = phoneNumber.trim()
    val trimmedEmail = email.trim()

    val payer = if (trimmedPhone.isNotEmpty() || trimmedEmail.isNotEmpty()) {
      val phone = if (trimmedPhone.isBlank()) {
        null
      } else {
        val phone = Phone.parse(trimmedPhone)!!
        ContactPhone(countryCode = phone.countryCode, nationalNumber = phone.nationalNumber, fullNumber = phone.fullNumber, country = phone.countryIso)
      }

      PayerData(
        contactPhone = phone,
        contactEmail = trimmedEmail.takeIf { it.isNotEmpty() }
      )
    } else null

    val request = OrderRequest(
      totalAmount = TotalAmount(baseUnits = amount, currency = currency),
      referenceNumber = Random.nextInt(1000, 9999).toString(),
      payer = payer
    )

    val requestBody = json.encodeToString(request)
      .toRequestBody("application/json".toMediaType())

    val httpRequest = Request.Builder()
      .url("$baseUrl/order")
      .addHeader("Authorization", "Bearer $token")
      .addHeader("Content-Type", "application/json")
      .post(requestBody)
      .build()

    val response = client.newCall(httpRequest).execute()
    if (!response.isSuccessful) {
      throw Exception("HTTP ${response.code}: ${response.message}")
    }

    val responseBody = response.body?.string() ?: throw Exception("Empty response")
    json.decodeFromString<OrderResponse>(responseBody)
  }
}

fun getBaseUrlFor(circuit: PaymentEnvironment) = when (circuit) {
  PaymentEnvironment.Development -> "https://cardium-cpg-dev.guavapay.com"
  PaymentEnvironment.Sandbox -> "https://sandbox-pgw.myguava.com"
  PaymentEnvironment.Production -> "https://api-pgw.myguava.com"
}
