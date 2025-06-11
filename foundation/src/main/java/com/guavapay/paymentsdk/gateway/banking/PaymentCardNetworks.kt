package com.guavapay.paymentsdk.gateway.banking

import androidx.annotation.DrawableRes
import com.guavapay.paymentsdk.R

enum class PaymentCardNetworks(@DrawableRes val imageres: Int) {
  VISA(R.drawable.ic_logo_cn_visa),
  MASTERCARD(R.drawable.ic_logo_cn_mastercard),
  UNIONPAY(R.drawable.ic_logo_cn_unionpay),
  DISCOVER(R.drawable.ic_logo_cn_discover),
  AMEX(R.drawable.ic_logo_cn_amex),
  DINERS_CLUB(R.drawable.ic_logo_cn_dinersclub)
}