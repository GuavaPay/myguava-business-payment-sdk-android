package com.guavapay.paymentsdk.gateway.banking

import com.guavapay.paymentsdk.network.serializers.PaymentCardCategorySerializer
import kotlinx.serialization.Serializable

@Serializable(with = PaymentCardCategorySerializer::class)
enum class PaymentCardCategory { DEBIT, CREDIT, PREPAID }
