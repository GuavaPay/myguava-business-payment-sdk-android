package com.guavapay.paymentsdk

import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayPayload

internal class LibraryState(
  var payload: PaymentGatewayPayload? = null,
  var analytics: PaymentAnalyticsState = PaymentAnalyticsState(),
  var device: Device = Device(),
) {
  inline fun payload() = payload ?: throw IllegalStateException("You can't access to gateway payload until not initialized!")

  data class Device(val ip: String? = null)

  data class PaymentAnalyticsState(
    val requestId: String? = null,
    val merchantName: String? = null,
    val paymentMethod: String? = null,
  )
}
