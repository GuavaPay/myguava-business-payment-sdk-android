@file:Suppress("unused")

package com.guavapay.paymentsdk.platform.coroutines

import com.guavapay.paymentsdk.logging.e
import com.guavapay.paymentsdk.platform.reflection.CallerFactory
import com.guavapay.paymentsdk.platform.threading.ThreadFactory
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

internal class CoroutineUnit {
  val executors = Executorz(); inner class Executorz {
    val common: ExecutorService = Executors.newCachedThreadPool(ThreadFactory("common"))

    fun coretimeout(threads: Int, tf: java.util.concurrent.ThreadFactory, ttl: Long = 30, ttlUnit: TimeUnit = TimeUnit.SECONDS) = ThreadPoolExecutor(
      threads, threads, ttl, ttlUnit, LinkedBlockingQueue(), tf
    ).apply { allowCoreThreadTimeOut(true) }
  }

  val dispatchers = Dispatchers(); inner class Dispatchers {
    val common = executors.common.asCoroutineDispatcher()
    val main = kotlinx.coroutines.Dispatchers.Main.immediate
  }

  val elements = Elements(); inner class Elements {
    val named get() = CoroutineName(with (CallerFactory.Types.Method) { CallerFactory.caller(1) })
  }

  val handlers = Handlers(); inner class Handlers {
    val metrica = ExceptionHandler { -> }
    val logcat = ExceptionHandler { c, e -> e(template(c, e), e) }

    inline fun <reified T : Throwable> typed(crossinline handler: () -> Any?) = typed<T> { _ -> handler() }
    inline fun <reified T : Throwable> typed(crossinline handler: (T) -> Any?) = ExceptionHandler { error -> if (error is T) handler(error) }

    private fun template(c: CoroutineContext, t: Throwable) = "An exception occurred in coroutine \"${c.coroutine()}\" with message: ${t.message}"
    private fun CoroutineContext.coroutine() = get(CoroutineName)?.name ?: "<unknown>"
  }

  val scopes = Scopes(); inner class Scopes {
    val untethered = CoroutineScope(SupervisorJob() + dispatchers.common + CompositeExceptionHandler(handlers.logcat, handlers.metrica))
  }
}
