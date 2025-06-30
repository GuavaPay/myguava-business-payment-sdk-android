package com.guavapay.paymentsdk.integrations.remote

import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.integrations.RunIntegration
import com.guavapay.paymentsdk.network.services.OrderApi.Models.ContinuePaymentRequest

internal suspend fun RemoteContinuePayment(lib: LibraryUnit, orderId: String, request: ContinuePaymentRequest) =
  RunIntegration(lib, 10000) { lib.network.services.order.continuePayment(orderId = orderId, request = request) }