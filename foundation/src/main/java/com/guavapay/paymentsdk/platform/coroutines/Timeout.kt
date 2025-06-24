package com.guavapay.paymentsdk.platform.coroutines

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

suspend inline fun <T, reified E : Exception> timeouting(
  timeoutMs: Long,
  crossinline factory: (TimeoutCancellationException) -> E,
  crossinline block: suspend () -> T
) = runCatching { withTimeout(timeoutMs) { block() } }.recoverCatching { cause ->
  when (cause) {
    is TimeoutCancellationException -> throw factory(cause)
    else -> throw cause
  }
}.getOrThrow()
