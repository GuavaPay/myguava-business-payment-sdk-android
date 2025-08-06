@file:Suppress("RedundantInnerClassModifier")

package com.guavapay.paymentsdk.network

import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.gateway.banking.PaymentCardCategory
import com.guavapay.paymentsdk.gateway.banking.PaymentCardScheme
import com.guavapay.paymentsdk.gateway.banking.PaymentCircuit
import com.guavapay.paymentsdk.logging.d
import com.guavapay.paymentsdk.logging.i
import com.guavapay.paymentsdk.network.serializers.BigDecimalSerializer
import com.guavapay.paymentsdk.network.serializers.CurrencySerializer
import com.guavapay.paymentsdk.network.serializers.PaymentCardCategorySerializer
import com.guavapay.paymentsdk.network.serializers.PaymentCardSchemeSerializer
import com.guavapay.paymentsdk.network.services.BindingsApi
import com.guavapay.paymentsdk.network.services.OrderApi
import com.guavapay.paymentsdk.network.ssevents.SseClient
import com.guavapay.paymentsdk.platform.function.lazy
import com.guavapay.paymentsdk.platform.manifest.manifestFields
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import okhttp3.Cache
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.math.BigDecimal
import java.util.Currency
import java.util.UUID
import java.util.concurrent.TimeUnit.SECONDS

internal class NetworkUnit(private val lib: LibraryUnit) {
  private val dispatcher = Dispatcher(lib.coroutine.executors.common)
  private val cache = Cache(lib.context.cacheDir, 5 * 1024 * 1024 /* 5 MiB */)

  val json = Json(); inner class Json() {
    val unspecified = Json {
      ignoreUnknownKeys = true
      encodeDefaults = true
      serializersModule = SerializersModule {
        contextual(BigDecimal::class, BigDecimalSerializer)
        contextual(Currency::class, CurrencySerializer)
        contextual(PaymentCardCategory::class, PaymentCardCategorySerializer)
        contextual(PaymentCardScheme::class, PaymentCardSchemeSerializer)
      }
    }
  }

  val clients = Clients(); inner class Clients() {
    val apikey get() = lib.state.payload().sessionToken

    val interceptors = Interceptors(); inner class Interceptors() {
      fun authentication() = Interceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
          .header("Authorization", "Bearer ${clients.apikey}")
          .header("Request-ID", UUID.randomUUID().toString())

        chain.proceed(request.build())
      }

      fun logging() = HttpLoggingInterceptor(::d).apply { setLevel(BODY).also { redactHeader("Authorization") } }
    }

    val authorized = client(interceptors.authentication(), interceptors.logging())
    val sse = client(interceptors.authentication())

    val unspecified = client()
  }

  val services = Services(); inner class Services() {
    private val baseUrl by lazy { when (lib.state.payload().circuit) {
      PaymentCircuit.Development -> "https://cardium-cpg-dev.guavapay.com"
      PaymentCircuit.Sandbox -> "https://sandbox-pgw.myguava.com"
      PaymentCircuit.Production -> "https://api-pgw.myguava.com"
      null -> lib.context.manifestFields().baseUrl
    } }

    val order by lazy { retrofit<OrderApi>(baseUrl, clients.authorized) }
    val bindings by lazy { retrofit<BindingsApi>(baseUrl, clients.authorized) }
  }

  val sse = SSE(); inner class SSE() {
    val client by lazy { SseClient(clients.sse) }
  }

  private inline fun <reified T> retrofit(
    baseUrl: String,
    client: OkHttpClient,
    factory: Converter.Factory = json.unspecified.asConverterFactory("application/json; charset=UTF-8".toMediaType()),
  ): T = Retrofit.Builder()
    .baseUrl(baseUrl)
    .addConverterFactory(factory)
    .client(client)
    .build().create(T::class.java)
    .also { i("${T::class.simpleName} retrofit API has successfully created") }

  private fun client(vararg interceptor: Interceptor): OkHttpClient {
    val builder = Builder().dispatcher(dispatcher).cache(cache).retryOnConnectionFailure(true).timeouts().redirects()
    interceptor.forEach(builder::addInterceptor)
    val client = builder.build()
    return client
  }

  private fun Builder.timeouts(timeout: Long = 30) = connectTimeout(timeout, SECONDS).callTimeout(timeout, SECONDS).readTimeout(timeout, SECONDS).writeTimeout(timeout, SECONDS)
  private fun Builder.redirects() = followRedirects(false).followSslRedirects(false)
}