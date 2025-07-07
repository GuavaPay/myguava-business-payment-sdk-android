package com.guavapay.paymentsdk

import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayPayload

internal class LibraryState(
  var payload: PaymentGatewayPayload? = null,
  var device: Device = Device()
) {
  inline fun payload() = payload ?: throw IllegalStateException("You can't access to gateway payload until not initialized!")

  data class Device(val ip: String? = null)
}
