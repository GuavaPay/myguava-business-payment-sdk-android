package com.guavapay.paymentsdk.demo

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil

class Phone(val countryCode: String, val nationalNumber: String, val fullNumber: String, val countryIso: String) {
  companion object {
    fun parse(raw: String): Phone? {
      val trimmed = raw.replace("\\s".toRegex(), "")
      if (trimmed.isEmpty()) return null

      val util = PhoneNumberUtil.getInstance()

      return try {
        val num = util.parse(trimmed, null)

        Phone(
          countryCode = num.countryCode.toString(),
          nationalNumber = num.nationalNumber.toString(),
          fullNumber = util.format(num, PhoneNumberUtil.PhoneNumberFormat.E164),
          countryIso = util.getRegionCodeForCountryCode(num.countryCode)
        )
      } catch (e: NumberParseException) {
        null
      }
    }
  }
}