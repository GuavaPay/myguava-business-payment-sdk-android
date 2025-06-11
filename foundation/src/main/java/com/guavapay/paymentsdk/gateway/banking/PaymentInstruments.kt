package com.guavapay.paymentsdk.gateway.banking

import java.io.Serializable

data class PaymentInstruments(val methods: Set<PaymentMethod>) : Serializable {
  inline fun <reified T : PaymentMethod> instrument() = methods.find { it is T } as? T
}