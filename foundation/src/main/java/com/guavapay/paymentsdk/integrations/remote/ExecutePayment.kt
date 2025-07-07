package com.guavapay.paymentsdk.integrations.remote

import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.integrations.RunIntegration
import com.guavapay.paymentsdk.network.services.OrderApi.Models.ExecutePaymentRequest

internal suspend fun RemoteExecutePayment(lib: LibraryUnit, orderId: String, request: ExecutePaymentRequest) =
  RunIntegration(lib, 60000) { lib.network.services.order.executePayment(orderId = orderId, request = request) }