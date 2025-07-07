package com.guavapay.paymentsdk.demo

import com.guavapay.paymentsdk.gateway.banking.PaymentCircuit
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

private val client = OkHttpClient().newBuilder().callTimeout(30L, TimeUnit.SECONDS).connectTimeout(30L, TimeUnit.SECONDS).readTimeout(30L, TimeUnit.SECONDS).writeTimeout(30L, TimeUnit.SECONDS).addInterceptor(HttpLoggingInterceptor()).build()
private val json = Json { ignoreUnknownKeys = true }

suspend fun createOrder(baseUrl: String, token: String, amount: Double, currency: String, phoneNumber: String, email: String): OrderResponse {
  return withContext(Dispatchers.IO) {
    val trimmedPhone = phoneNumber.trim()
    val trimmedEmail = email.trim()

    val payer = if (trimmedPhone.isNotEmpty() || trimmedEmail.isNotEmpty()) {
      PayerData(
        contactPhone = if (trimmedPhone.isNotEmpty()) { // this shit is temporary there. (currencyly satisfy BE number acceptance criteria.)
          val (countryCode, nationalNumber) = if (trimmedPhone.startsWith("+")) {
            val phoneDigits = trimmedPhone.substring(1)
            val countryCodeLength = when {
              phoneDigits.length > 10 -> 3
              phoneDigits.length > 7 -> 2
              else -> 1
            }
            val country = phoneDigits.take(countryCodeLength)
            val national = phoneDigits.substring(countryCodeLength)
            country to national
          } else {
            "1" to trimmedPhone
          }

          ContactPhone(countryCode = countryCode, nationalNumber = nationalNumber, fullNumber = trimmedPhone, country = "US"
        } else null,
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

fun getBaseUrlFor(circuit: PaymentCircuit) = when (circuit) {
  PaymentCircuit.Development -> "https://cardium-cpg-dev.guavapay.com"
  PaymentCircuit.Sandbox -> "https://sandbox-pgw.myguava.com"
  PaymentCircuit.Production -> "https://api-pgw.myguava.com"
}