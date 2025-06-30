package com.guavapay.paymentsdk

import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayPayload

internal class LibraryState(var payload: PaymentGatewayPayload? = null) {
  inline fun payload() = payload ?: throw IllegalStateException("You can't access to gateway payload until not initialized!")
}
