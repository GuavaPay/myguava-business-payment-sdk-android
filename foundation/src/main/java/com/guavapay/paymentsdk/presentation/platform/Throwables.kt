package com.guavapay.paymentsdk.presentation.platform

import kotlinx.coroutines.CancellationException

internal inline fun retrow(t: Throwable) {
  if (t !is CancellationException) throw t
}