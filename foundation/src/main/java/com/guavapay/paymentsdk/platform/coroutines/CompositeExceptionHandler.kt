package com.guavapay.paymentsdk.platform.coroutines

import kotlinx.coroutines.CoroutineExceptionHandler

internal class CompositeExceptionHandler(
  private vararg val handlers: CoroutineExceptionHandler,
  handler: CoroutineExceptionHandler = CoroutineExceptionHandler { c, e -> handlers.forEach { it.handleException(c, e) } }
) : CoroutineExceptionHandler by handler
