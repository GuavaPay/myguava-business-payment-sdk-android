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
import io.sentry.SentryLevel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.Request
import java.io.IOException

internal fun RemoteOrderSubscription(lib: LibraryUnit) = flow {
  val payload = lib.state.payload()
  val baseUrl = lib.context.manifestFields().baseUrl
  val url = "$baseUrl/sse/order/${payload.orderId}?merchant-included=true&payment-requirements-included=true&transactions-included=true"

  var sseAttempts = 0
  var fallbackFailures: Int
  var isInFallback = false

  suspend fun trySSEConnection(): Boolean {
    return runCatching {
      val req = Request.Builder()
        .url(url)
        .header("Authorization", "Bearer ${payload.sessionToken}")
        .header("Accept", "text/event-stream;charset=UTF-8")
        .build()

      lib.network.sse.client.events(req).collect { event: SseEvent ->
        when (event) {
          is SseEvent.Open -> {
            i("SSE connection opened for order ${payload.orderId}")
            sseAttempts = 0
            isInFallback = false
          }
          is SseEvent.Message -> {
            if (!event.data.contains("PAYMENT_REQUIREMENTS_UPDATED")) {
              emit(lib.network.json.unspecified.decodeFromString<GetOrderResponse>(event.data))
            }
          }
          is SseEvent.Closed -> {
            i("SSE connection closed for order ${payload.orderId}, reconnecting immediately...")
            sseAttempts++
            return@collect
          }
          is SseEvent.Failure -> {
            val code = event.code

            lib.metrica.event(
              message = "SSE Failed",
              level = SentryLevel.ERROR,
              tags = mapOf("category" to "sdk.network"),
              contexts = mapOf("error" to mapOf("code" to code, "message" to event.message))
            )

            if (code == null || code >= 500) {
              throw SseException(code ?: 999)
            } else if (code in 400..499) {
              throw SseException(code)
            } else {
              throw SseException(999)
            }
          }
        }
      }
      true
    }.getOrElse { error ->
      lib.metrica.event(
        message = "SSE Attempt Failed",
        level = SentryLevel.ERROR,
        tags = mapOf("category" to "sdk.network"),
        contexts = mapOf("network" to mapOf("attempt" to sseAttempts + 1))
      )

      lib.coroutine.handlers.logcat.handler(currentCoroutineContext(), error)
      lib.coroutine.handlers.metrica.handler(currentCoroutineContext(), error)

      if (error is SseException && error.code in 400..499) {
        throw error
      }

      sseAttempts++
      e("SSE connection attempt $sseAttempts failed for order ${payload.orderId}")
      false
    }
  }

  suspend fun runPollingFallback() {
    e("Entering polling fallback for order ${payload.orderId}")

    lib.metrica.event(
      message = "SSE Fallback Polling",
      level = SentryLevel.WARNING,
      tags = mapOf("category" to "sdk.network"),
    )

    isInFallback = true
    fallbackFailures = 0

    val backgroundJob = lib.coroutine.scopes.untethered.launch {
      while (currentCoroutineContext().isActive && isInFallback) {
        delay(30000)

        if (isInFallback) {
          i("Attempting background SSE reconnection for order ${payload.orderId}")
          val tempAttempts = sseAttempts
          sseAttempts = 0

          if (trySSEConnection()) {
            i("Background SSE reconnection successful for order ${payload.orderId}")
            return@launch
          }

          sseAttempts = tempAttempts
        }
      }
    }

    while (currentCoroutineContext().isActive && isInFallback) {
      runCatching {
        val response = RunIntegration(lib) {
          lib.network.services.order.getOrder(
            orderId = payload.orderId,
            merchantIncluded = true,
            paymentRequirementsIncluded = true,
            transactionsIncluded = true,
          )
        }

        emit(response)
        fallbackFailures = 0
        delay(10000)
      }.getOrElse { error ->
        lib.coroutine.handlers.logcat.handler(currentCoroutineContext(), error)
        lib.coroutine.handlers.metrica.handler(currentCoroutineContext(), error)

        fallbackFailures++
        e("Polling fallback failure $fallbackFailures for order ${payload.orderId}")

        lib.metrica.event(
          message = "SSE Fallback Polling failed",
          level = SentryLevel.WARNING,
          tags = mapOf("category" to "sdk.network"),
          contexts = mapOf("network" to mapOf("failure" to fallbackFailures))
        )

        if (fallbackFailures >= 5) {
          lib.metrica.event(
            message = "SSE Fallback Polling Max Retries Exceed",
            level = SentryLevel.ERROR,
            tags = mapOf("category" to "sdk.network"),
            contexts = mapOf("network" to mapOf("failures" to fallbackFailures))
          )

          backgroundJob.cancel()
          throw IOException("Connection error: Maximum polling fallback failures reached")
        }

        delay(2000)
      }
    }

    backgroundJob.cancel()
  }

  while (currentCoroutineContext().isActive) {
    if (sseAttempts < 3 && !isInFallback) {
      if (trySSEConnection()) {
        continue
      }
    } else if (!isInFallback) {
      e("Maximum SSE connection attempts reached for order ${payload.orderId}, switching to polling fallback")
      lib.metrica.event(
        message = "SSE Max Retries Exceed",
        level = SentryLevel.ERROR,
        tags = mapOf("category" to "sdk.network"),
        contexts = mapOf("network" to mapOf("attempts" to sseAttempts))
      )
      runPollingFallback()
      break
    }

    if (sseAttempts < 3) {
      continue
    }
  }
}