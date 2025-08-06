@file:Suppress("FunctionName")

package com.guavapay.paymentsdk.integrations.remote

import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.integrations.RunIntegration
import com.guavapay.paymentsdk.network.services.OrderApi.Models.CardRangeRequest

internal suspend fun RemoteCardRangeData(lib: LibraryUnit, pan: String) =
  RunIntegration(lib) { lib.network.services.order.getCardRangeData(CardRangeRequest(pan)) }