package com.guavapay.paymentsdk.integrations

import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.integrations.IntegrationException.ClientError
import com.guavapay.paymentsdk.integrations.IntegrationException.NoResponseError
import com.guavapay.paymentsdk.integrations.IntegrationException.ServerError
import com.guavapay.paymentsdk.integrations.IntegrationException.TimeoutError
import com.guavapay.paymentsdk.integrations.IntegrationException.UnqualifiedError
import com.guavapay.paymentsdk.platform.coroutines.timeouting
import kotlinx.coroutines.withContext
import retrofit2.Response
import com.guavapay.paymentsdk.logging.e
import com.guavapay.paymentsdk.logging.w
import com.guavapay.paymentsdk.logging.d

internal suspend inline fun <reified T> RunIntegration(lib: LibraryUnit, crossinline op: suspend () -> Response<T>): T = RunIntegration(lib, 4000, op)

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
      retry(3) {
        runCatching {
          map(op())
        }.onFailure {
          lib.coroutine.handlers.logcat.handler(coroutineContext, it)
          lib.coroutine.handlers.metrica.handler(coroutineContext, it)
        }.getOrThrow()
      }
    }
  }
}

internal sealed class IntegrationException(message: String, cause: Throwable? = null) : Exception(message, cause) {
  object NoResponseError : IntegrationException("No response from server (204)", null)
  class TimeoutError(message: String, cause: Throwable? = null) : IntegrationException(message, cause)
  class ClientError(override val message: String, val code: Int, cause: Throwable? = null) : IntegrationException(message, cause)
  class ServerError(override val message: String, val code: Int, cause: Throwable? = null) : IntegrationException(message, cause)
  class UnqualifiedError(message: String, cause: Throwable? = null) : IntegrationException(message, cause)
}

private fun <T> map(response: Response<T>) = when {
  response.isSuccessful -> response.body() ?: if (response.code() == 204) throw NoResponseError else throw UnqualifiedError("no response when expected")
  response.code() in 400..499 -> throw ClientError(message = response.message().ifBlank { "Client error" }, code = response.code())
  response.code() in 500..599 -> throw ServerError(message = response.message().ifBlank { "Server error" }, code = response.code())
  else -> throw UnqualifiedError("returned unexpected HTTP ${response.code()}: ${response.message()}")
}

private suspend inline fun <reified T> retry(attempts: Int, crossinline operation: suspend (attempt: Int) -> T): T {
  var record: Throwable? = null

  repeat(attempts) { attempt ->
    try {
      return operation(attempt)
    } catch (e: Throwable) {
      record = e

      if (attempt == attempts - 1 || e !is ServerError) {
        e("An error occurred while executing the integration operation for gathering ${T::class.simpleName}", e)
        throw e
      } else {
        w("An server error occurred while executing the integration operation for gathering ${T::class.simpleName} (attempt: $attempt), retrying...")
      }
    }
  }

  throw record!! /* null-forgiveness never will bang in prod. */
}