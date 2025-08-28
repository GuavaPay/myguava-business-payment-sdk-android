@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.guavapay.paymentsdk.integrations

import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.integrations.IntegrationException.ClientError
import com.guavapay.paymentsdk.integrations.IntegrationException.NoResponseError
import com.guavapay.paymentsdk.integrations.IntegrationException.ServerError
import com.guavapay.paymentsdk.integrations.IntegrationException.TimeoutError
import com.guavapay.paymentsdk.integrations.IntegrationException.UnqualifiedError
import com.guavapay.paymentsdk.logging.d
import com.guavapay.paymentsdk.logging.e
import com.guavapay.paymentsdk.logging.w
import com.guavapay.paymentsdk.platform.coroutines.timeouting
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.UUID
import kotlin.coroutines.coroutineContext

internal suspend inline fun <reified T> RunLocalIntegration(lib: LibraryUnit, crossinline op: suspend () -> T): T = RunLocalIntegration(lib, 4000, op)
internal suspend inline fun <reified T> RunIntegration(lib: LibraryUnit, crossinline op: suspend () -> Response<T>): T = RunIntegration(lib, 30_000, op)

internal suspend inline fun <reified T> RunIntegration(lib: LibraryUnit, timeoutMs: Long, crossinline op: suspend () -> Response<T>): T {
  return withContext(lib.coroutine.dispatchers.common + lib.coroutine.elements.named) {
    d("Starting integration operation for gathering ${T::class.simpleName}")

    timeouting<T, TimeoutError>(
      timeoutMs = timeoutMs,
      factory = { cause ->
        e("Integration operation for gathering ${T::class.simpleName} timed out", cause)
        TimeoutError("request timed out", cause)
      }
    ) {
      retry(lib, 3) { attempt ->
        runCatching {
          map(lib,op())
        }.onFailure {
          lib.coroutine.handlers.logcat.handler(coroutineContext, it)
        }.getOrThrow()
      }
    }
  }
}

internal suspend inline fun <reified T> RunLocalIntegration(lib: LibraryUnit, timeoutMs: Long, crossinline op: suspend () -> T): T {
  return withContext(lib.coroutine.dispatchers.common + lib.coroutine.elements.named) {
    d("Starting local integration operation for gathering ${T::class.simpleName}")

    timeouting<T, TimeoutError>(
      timeoutMs = timeoutMs,
      factory = { cause ->
        e("Local integration operation for gathering ${T::class.simpleName} timed out", cause)
        TimeoutError("local operation timed out", cause)
      }
    ) {
      runCatching {
        op()
      }.onFailure {
        e("An error occurred while executing the local integration operation for gathering ${T::class.simpleName}", it)
        lib.coroutine.handlers.logcat.handler(coroutineContext, it)
        lib.coroutine.handlers.metrica.handler(coroutineContext, it)
      }.getOrThrow()
    }
  }
}

internal sealed class IntegrationException(message: String, cause: Throwable? = null) : Exception(message, cause) {
  class NoResponseError : IntegrationException("No response from server (204)") { private fun readResolve(): Any = NoResponseError() }
  class TimeoutError(message: String, cause: Throwable? = null) : IntegrationException(message, cause)
  class ClientError(override val message: String, val code: Int, cause: Throwable? = null) : IntegrationException(message, cause)
  class ServerError(override val message: String, val code: Int, cause: Throwable? = null) : IntegrationException(message, cause)
  class UnqualifiedError(message: String, cause: Throwable? = null) : IntegrationException(message, cause)
}

private inline fun <reified T> map(lib: LibraryUnit, response: Response<T>) = when {
  response.isSuccessful -> response.body() ?: when {
    response.code() == 204 -> throw NoResponseError()
    T::class == Unit::class -> Unit as T
    else -> throw UnqualifiedError("no response when expected")
  }
  response.code() in 400..499 -> throw ClientError(message = response.message().ifBlank { "Client error" }, code = response.code())
  response.code() in 500..599 -> throw ServerError(message = response.message().ifBlank { "Server error" }, code = response.code())
  else -> throw UnqualifiedError("returned unexpected HTTP ${response.code()}: ${response.message()}")
}

private suspend inline fun <reified T> retry(lib: LibraryUnit, attempts: Int, crossinline operation: suspend (attempt: Int) -> T): T {
  var record: Throwable? = null

  repeat(attempts) { attempt ->
    try {
      return operation(attempt)
    } catch (e: Throwable) {
      record = e

      if (attempt == attempts - 1 || e !is ServerError) {
        e("An error occurred while executing the integration operation for gathering ${T::class.simpleName}", e)
        lib.coroutine.handlers.metrica.handler(currentCoroutineContext(), e)
        throw e
      } else {
        w("An server error occurred while executing the integration operation for gathering ${T::class.simpleName} (attempt: ${attempt + 1}), retrying...")
      }
    }
  }

  throw record!! /* null-forgiveness never will bang in prod. */
}