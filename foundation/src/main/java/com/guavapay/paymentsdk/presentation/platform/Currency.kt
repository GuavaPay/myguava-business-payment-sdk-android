package com.guavapay.paymentsdk.presentation.platform

import java.text.DecimalFormat.getCurrencyInstance
import java.util.Currency
import java.util.Currency.getInstance
import java.util.Locale

internal fun currencify(baseUnit: Number, currency: Currency, locale: Locale) =
  getCurrencyInstance(locale).apply { this.currency = currency }.format(baseUnit)

internal fun currencify(baseUnit: Number, currency: String, locale: Locale) =
  currencify(baseUnit, getInstance(currency), locale)