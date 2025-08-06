package com.guavapay.paymentsdk.demo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class ContactPhone(@SerialName("countryCode") val countryCode: String, @SerialName("nationalNumber") val nationalNumber: String, @SerialName("fullNumber") val fullNumber: String, @SerialName("country") val country: String? = null)
@Serializable data class PayerData(@SerialName("contactPhone") val contactPhone: ContactPhone? = null, @SerialName("contactEmail") val contactEmail: String? = null, val address: Address = Address(), @SerialName("lastName") val lastName: String = "Doe", @SerialName("firstName") val firstName: String? = "John")
@Serializable data class Address(
  @SerialName("country") val country: String = "GB",
  @SerialName("state") val state: String = "BC",
  @SerialName("zipCode") val zipCode: String = "NW1",
  @SerialName("city") val city: String = "London",
  @SerialName("addressLine1") val addressLine1: String = "Baker st, 1",
  @SerialName("addressLine2") val addressLine2: String = "Baker st, 1",
)
@Serializable data class OrderRequest(@SerialName("totalAmount") val totalAmount: TotalAmount, @SerialName("referenceNumber") val referenceNumber: String, @SerialName("payerId") val payerId: String = "2A4E99BA-AACF-47E8-9D1B-F4FC95037DE9",  @SerialName("payer") val payer: PayerData? = null)
@Serializable data class TotalAmount(val baseUnits: Double, val currency: String)
@Serializable data class OrderResponse(val order: Order)
@Serializable data class Order(val id: String, val referenceNumber: String, val status: String, val totalAmount: ResponseTotalAmount, val expirationDate: String, val sessionToken: String)
@Serializable data class ResponseTotalAmount(val baseUnits: Double, val currency: String, val localized: String, val minorSubunits: Long)