package com.guavapay.paymentsdk.platform.function

import kotlin.LazyThreadSafetyMode.NONE

internal fun <T, R> R.lazy(initializer: R.() -> T): Lazy<T> = lazy(NONE) { initializer() }
internal fun <T> lazy(initializer: () -> T): Lazy<T> = lazy(NONE) { initializer() }
