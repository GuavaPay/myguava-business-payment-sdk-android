@file:Suppress("FunctionName")

package com.guavapay.paymentsdk.integrations.remote

import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.integrations.RunIntegration
import com.guavapay.paymentsdk.logging.e
import com.guavapay.paymentsdk.logging.i
import com.guavapay.paymentsdk.network.services.OrderApi.Models.GetOrderResponse
import com.guavapay.paymentsdk.network.ssevents.SseEvent
import com.guavapay.paymentsdk.network.ssevents.SseException
import com.guavapay.paymentsdk.platform.manifest.manifestFields
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import okhttp3.Request

internal fun RemoteOrderSubscription(lib: LibraryUnit) = flow {
  val payload = lib.state.payload()

  runCatching {
    val baseUrl = lib.context.manifestFields().baseUrl
    val url = "$baseUrl/sse/order/${payload.orderId}?merchant-included=true&payment-requirements-included=true"

    while (currentCoroutineContext().isActive) {
      val req = Request.Builder()
        .url(url)
        .header("Authorization", "Bearer ${payload.sessionToken}")
        .header("Accept", "text/event-stream;charset=UTF-8")
        .build()

      lib.network.sse.client.events(req).collect { event: SseEvent ->
        when (event) {
          is SseEvent.Open -> i("SSE connection opened for order ${payload.orderId}")
          is SseEvent.Message -> emit(lib.network.json.unspecified.decodeFromString<GetOrderResponse>(event.data))
          is SseEvent.Closed -> {
            i("SSE connection closed for order ${payload.orderId}, reconnecting...")
            delay(3000)
            return@collect
          }
          is SseEvent.Failure -> {
            val code = event.code
            if (code == null || code >= 500) { // Network error || Backend error
              throw SseException(code ?: 999 /* Network error */)
            } else if (code in 400..499) { // Only client error
              throw SseException(500)
            } else { // Honestly, I don't know what to do with this case, well, return network error (999)
              throw SseException(999)
            }
          }
        }
      }
    }
  }.getOrElse {
    lib.coroutine.handlers.logcat.handler(currentCoroutineContext(), it)
    lib.coroutine.handlers.metrica.handler(currentCoroutineContext(), it)

    if (it is SseException && it.code in 400..499) {
      throw it
    } else if (it is SseException) {
      e("An error occurred while subscribing to order updates via SSE, switching to polling fallback")

      while (currentCoroutineContext().isActive) {
        val response = RunIntegration(lib) {
          lib.network.services.order.getOrder(orderId = payload.orderId, merchantIncluded = true, paymentRequirementsIncluded = true)
        }

        emit(response)
        delay(3000)
      }
    } else {
      throw it
    }
  }
}