package com.guavapay.paymentsdk.demo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class ContactPhone(@SerialName("countryCode") val countryCode: String, @SerialName("nationalNumber") val nationalNumber: String, @SerialName("fullNumber") val fullNumber: String, @SerialName("country") val country: String? = null)
@Serializable data class PayerData(@SerialName("contactPhone") val contactPhone: ContactPhone? = null, @SerialName("contactEmail") val contactEmail: String? = null)
@Serializable data class OrderRequest(@SerialName("totalAmount") val totalAmount: TotalAmount, @SerialName("referenceNumber") val referenceNumber: String, @SerialName("payer") val payer: PayerData? = null)
@Serializable data class TotalAmount(val baseUnits: Double, val currency: String)
@Serializable data class OrderResponse(val order: Order)
@Serializable data class Order(val id: String, val referenceNumber: String, val status: String, val totalAmount: ResponseTotalAmount, val expirationDate: String, val sessionToken: String)
@Serializable data class ResponseTotalAmount(val baseUnits: Double, val currency: String, val localized: String, val minorSubunits: Long)