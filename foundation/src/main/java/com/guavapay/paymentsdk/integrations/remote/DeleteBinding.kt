@file:Suppress("FunctionName")

package com.guavapay.paymentsdk.integrations.remote

import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.integrations.RunIntegration

internal suspend fun RemoteDeleteBinding(lib: LibraryUnit, bindingId: String) =
  RunIntegration(lib) { lib.network.services.bindings.deleteBinding(bindingId) }