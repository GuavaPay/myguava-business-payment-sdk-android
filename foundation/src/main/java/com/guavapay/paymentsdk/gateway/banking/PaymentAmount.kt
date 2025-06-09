package com.guavapay.paymentsdk.gateway.banking

import java.io.Serializable
import java.math.BigDecimal
import java.text.NumberFormat.getCurrencyInstance
import java.util.Currency
import java.util.Locale

data class PaymentAmount(val value: BigDecimal, val currency: Currency) : Serializable {
  fun format(locale: Locale = Locale.getDefault()): String {
    val formatter = getCurrencyInstance(locale)
    formatter.currency = currency
    return formatter.format(value)
  }
}