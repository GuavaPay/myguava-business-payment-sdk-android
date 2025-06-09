package com.guavapay.paymentsdk.gateway.banking

import java.io.Serializable

data class PaymentInstruments(val methods: Set<PaymentMethod> = setOf()) : Serializable