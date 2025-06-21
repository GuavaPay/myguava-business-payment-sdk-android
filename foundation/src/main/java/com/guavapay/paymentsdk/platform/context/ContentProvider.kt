package com.guavapay.paymentsdk.platform.context

import androidx.startup.Initializer

internal fun interface IsolatedInitializer<T> : Initializer<T> {
  override fun dependencies(): List<Class<out Initializer<*>?>?> = emptyList()
}