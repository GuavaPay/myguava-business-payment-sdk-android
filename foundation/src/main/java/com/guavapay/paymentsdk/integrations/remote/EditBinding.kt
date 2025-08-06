@file:Suppress("FunctionName")

package com.guavapay.paymentsdk.integrations.remote

import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.integrations.RunIntegration
import com.guavapay.paymentsdk.network.services.BindingsApi.Models

internal suspend fun RemoteEditBinding(lib: LibraryUnit, bindingId: String, bindingName: String) =
  RunIntegration(lib) {
    lib.network.services.bindings.updateBinding(bindingId, Models.UpdateBindingRequest(bindingName))
  }