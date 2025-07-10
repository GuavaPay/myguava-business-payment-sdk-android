package com.guavapay.paymentsdk.gateway.banking

import com.guavapay.paymentsdk.R

enum class PaymentCardScheme(val image: Int, val pan: Int, val cvc: Int) {
  VISA(R.drawable.ic_logo_cn_visa, 16, 3),
  MASTERCARD(R.drawable.ic_logo_cn_mastercard, 16, 3),
  UNIONPAY(R.drawable.ic_logo_cn_unionpay, 16, 3),
  AMERICAN_EXPRESS(R.drawable.ic_logo_cn_amex, 15, 4),
  DINERS_CLUB(R.drawable.ic_logo_cn_dinersclub, 19, 3)
}