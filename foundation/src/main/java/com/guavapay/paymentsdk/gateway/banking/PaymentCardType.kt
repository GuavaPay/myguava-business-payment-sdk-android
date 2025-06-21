package com.guavapay.paymentsdk.gateway.banking

import com.guavapay.paymentsdk.network.serializers.PaymentCardTypeSerializer
import kotlinx.serialization.Serializable

@Serializable(with = PaymentCardTypeSerializer::class)
enum class PaymentCardType { DEBIT, CREDIT }
