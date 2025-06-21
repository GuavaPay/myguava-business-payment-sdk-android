package com.guavapay.paymentsdk.gateway.banking

import java.util.Locale
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks.AMEX
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks.DINERS_CLUB
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks.DISCOVER
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks.MASTERCARD
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks.UNIONPAY
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks.VISA
import com.guavapay.paymentsdk.gateway.banking.PaymentCardType.CREDIT
import com.guavapay.paymentsdk.gateway.banking.PaymentCardType.DEBIT
import com.guavapay.paymentsdk.gateway.vendors.googlepay.GPayAuthMethods
import com.guavapay.paymentsdk.gateway.vendors.googlepay.GPayAuthMethods.CRYPTOGRAM_3DS
import com.guavapay.paymentsdk.gateway.vendors.googlepay.GPayAuthMethods.PAN_ONLY
import com.guavapay.paymentsdk.gateway.vendors.googlepay.GPayEnvironment
import com.guavapay.paymentsdk.gateway.vendors.googlepay.GPayOrderType
import java.io.Serializable

sealed interface PaymentMethod : Serializable {
  data class PayPal(val unused: Unit = Unit) : PaymentMethod

  data class GooglePay(
    val environment: GPayEnvironment,
    val merchant: String,
    val authmethods: Set<GPayAuthMethods> = setOf(PAN_ONLY, CRYPTOGRAM_3DS),
    val networks: Set<PaymentCardNetworks> = setOf(VISA, MASTERCARD, AMEX, UNIONPAY, DISCOVER, DINERS_CLUB),
    val cardtypes: Set<PaymentCardType> = setOf(DEBIT, CREDIT),
    val ordertype: GPayOrderType = GPayOrderType.Pay,
    val locale: Locale? = null
  ) : PaymentMethod

  data class Card(
    val networks: Set<PaymentCardNetworks> = setOf(VISA, MASTERCARD, AMEX, UNIONPAY, DISCOVER, DINERS_CLUB),
    val cardtypes: Set<PaymentCardType> = setOf(DEBIT, CREDIT),
    val flags: Flags = Flags()
  ) : PaymentMethod {
    data class Flags(
      val allowScan: Boolean = true,
      val allowSaveCard: Boolean = true,
    ) : Serializable
  }

  data class SavedCard(val flags: Flags = Flags()) : PaymentMethod {
    data class Flags(
      val allowEdit: Boolean = true,
      val allowRemove: Boolean = true,
      val showUnavailable: Boolean = false,
    ) : Serializable
  }
}