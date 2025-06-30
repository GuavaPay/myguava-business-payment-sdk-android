package com.guavapay.paymentsdk

import com.guavapay.paymentsdk.platform.context.IsolatedInitializer
import com.guavapay.paymentsdk.logging.i

internal class LibraryZygote : IsolatedInitializer<LibraryUnit> by (IsolatedInitializer { LibraryUnit(it.applicationContext).initialize() }) {
  init { i("SDK Library zygote initialization started") }
}