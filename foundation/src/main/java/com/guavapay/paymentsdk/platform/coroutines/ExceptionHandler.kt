@file:Suppress("unused")

package com.guavapay.paymentsdk.platform.coroutines

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

internal class ExceptionHandler(val handler: (CoroutineContext, Throwable) -> Unit) : CoroutineExceptionHandler by CoroutineExceptionHandler(handler) {
  constructor(handler: (Throwable) -> Unit) : this({ _, e -> handler(e) })
  constructor(handler: () -> Unit) : this({ _ -> handler() })
}