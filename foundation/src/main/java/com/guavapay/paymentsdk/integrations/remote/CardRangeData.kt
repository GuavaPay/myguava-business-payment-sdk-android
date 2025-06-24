package com.guavapay.paymentsdk.integrations.remote

import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.integrations.RunIntegration
import com.guavapay.paymentsdk.network.services.OrderApi

internal suspend fun RemoteCardRangeData(lib: LibraryUnit, pan: String) =
  RunIntegration(lib, 4000) {
    lib.network.services.order.getCardRangeData(OrderApi.Models.CardRangeRequest(pan))
  }