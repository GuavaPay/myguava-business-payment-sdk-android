package com.guavapay.paymentsdk.gateway.banking

import java.io.Serializable

sealed interface PaymentMethod : Serializable {
  companion object { val Entries get() = setOf(PayPal, GooglePay, Card(), SavedCard()) }

  data object PayPal : PaymentMethod { private fun readResolve(): Any = PayPal }
  data object GooglePay : PaymentMethod { private fun readResolve(): Any = GooglePay }

  data class Card(val flags: Flags = Flags()) : PaymentMethod {
    data class Flags(val allowScan: Boolean = true) : Serializable
  }

  data class SavedCard(val flags: Flags = Flags()) : PaymentMethod {
    data class Flags(val allowEdit: Boolean = true, val allowRemove: Boolean = true, val showUnavailable: Boolean = false) : Serializable
  }
}