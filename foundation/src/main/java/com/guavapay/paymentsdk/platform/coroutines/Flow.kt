package com.guavapay.paymentsdk.platform.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.cancellation.CancellationException

class OnEachRecoverScope<T>(private val collector: FlowCollector<T>, val original: T) {
  private var handler: (suspend OnEachRecoverScope<T>.(Throwable) -> Unit)? = null
  internal var emitted: Boolean = false

  fun recover(block: suspend OnEachRecoverScope<T>.(Throwable) -> Unit) {
    handler = block
  }

  suspend fun emit(value: T) {
    collector.emit(value)
    emitted = true
  }

  internal suspend fun runRecover(cause: Throwable) {
    handler?.invoke(this, cause)
  }
}

fun <T> Flow<T>.onEachRecover(block: suspend OnEachRecoverScope<T>.() -> Unit) = flow {
  collect { value ->
    val scope = OnEachRecoverScope(this, value)
    try {
      scope.block()
      emit(value)
    } catch (e: Throwable) {
      if (e is CancellationException) throw e
      scope.runRecover(e)
      if (!scope.emitted) {
        // Просто игнорим ошибку, если в блоке recover не было вызова emit
      }
    }
  }
}