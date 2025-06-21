package com.guavapay.paymentsdk

import com.guavapay.paymentsdk.platform.context.IsolatedInitializer

internal class LibraryZygote : IsolatedInitializer<LibraryUnit> by (IsolatedInitializer { LibraryUnit(it.applicationContext).initialize() })