package com.guavapay.paymentsdk.network

import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.logging.d
import com.guavapay.paymentsdk.logging.i
import com.guavapay.paymentsdk.network.services.BindingsApi
import com.guavapay.paymentsdk.network.services.OrderApi
import com.guavapay.paymentsdk.network.ssevents.SseClient
import com.guavapay.paymentsdk.platform.manifest.manifestFields
import kotlinx.serialization.json.Json
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
import java.util.UUID
import java.util.concurrent.TimeUnit.SECONDS

internal class NetworkUnit(private val lib: LibraryUnit) {
  private val dispatcher = Dispatcher(lib.coroutine.executors.common)
  private val cache = Cache(lib.context.cacheDir, 5 * 1024 * 1024 /* 5 MiB */)

  private val apikey = lib.context.manifestFields().apikey

  val json = Json(); inner class Json() {
    val unspecified = Json { ignoreUnknownKeys = true }
  }

  val clients = Clients(); inner class Clients() {
    val authorized = client { chain ->
      val original = chain.request()
      val request = original.newBuilder()
        .header("Authorization", "Bearer $apikey")
        .header("Request-ID", UUID.randomUUID().toString())

      chain.proceed(request.build())
    }

    val unspecified = client()
  }

  val services = Services(); inner class Services() {
    private val baseUrl = lib.context.manifestFields().baseUrl
    val order = retrofit<OrderApi>(baseUrl, clients.authorized)
    val bindings = retrofit<BindingsApi>(baseUrl, clients.authorized)
  }

  val sse = SSE(); inner class SSE() {
    val client by lazy { SseClient(clients.authorized) }
  }

  private inline fun <reified T> retrofit(
    baseUrl: String,
    client: OkHttpClient,
    factory: Converter.Factory = json.unspecified.asConverterFactory("application/json; charset=UTF8".toMediaType()),
  ): T = Retrofit.Builder()
    .baseUrl(baseUrl)
    .addConverterFactory(factory)
    .client(client)
    .build().create(T::class.java)
    .also { i("${T::class.simpleName} retrofit API has successfully created") }

  private fun client(interceptor: Interceptor? = null): OkHttpClient {
    val log = HttpLoggingInterceptor(::d).apply { setLevel(BODY).also { redactHeader("Authorization") } }
    val builder = Builder().dispatcher(dispatcher).cache(cache).timeouts().redirects().addInterceptor(log)

    interceptor?.let(builder::addInterceptor)
    val client = builder.build()
    return client
  }

  private fun Builder.timeouts() = connectTimeout(30, SECONDS).callTimeout(30, SECONDS)
  private fun Builder.redirects() = followRedirects(false).followSslRedirects(false)
}