package com.guavapay.paymentsdk.gateway.banking

import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.network.serializers.PaymentCardNetworkSerializer
import kotlinx.serialization.Serializable

@Serializable(with = PaymentCardNetworkSerializer::class)
enum class PaymentCardNetwork(val image: Int, val cvc: Int) {
  VISA(R.drawable.ic_logo_cn_visa, 3),
  MASTERCARD(R.drawable.ic_logo_cn_mastercard, 3),
  UNIONPAY(R.drawable.ic_logo_cn_unionpay, 3),
  DISCOVER(R.drawable.ic_logo_cn_discover, 3),
  AMEX(R.drawable.ic_logo_cn_amex, 4),
  DINERS_CLUB(R.drawable.ic_logo_cn_dinersclub, 3)
}