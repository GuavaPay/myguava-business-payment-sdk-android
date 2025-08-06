@file:Suppress("FunctionName")

package com.guavapay.paymentsdk.integrations.remote

import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.integrations.RunIntegration

internal suspend fun RemoteGetBindings(lib: LibraryUnit) =
  RunIntegration(lib) { lib.network.services.bindings.getBindingList(0, 100) }