package com.guavapay.paymentsdk.network.services

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.*

interface BindingsApi {
  @GET("bindings")
  suspend fun getBindingList(
    @Query("page") page: Int? = null,
    @Query("size") size: Int? = null,
    @Query("sort") sort: String? = null,
    @Query("payer-id") payerId: String? = null,
    @Query("merchant-id") filterMerchantId: String? = null,
    @Query("id") bindingId: List<String>? = null,
    @Query("activity") activity: Boolean? = null,
    @Query("card-scheme") cardScheme: String? = null
  ): Response<Models.GetBindingListResponse>

  @PUT("binding/{bindingId}/activity")
  suspend fun changeBindingActivity(
    @Path("bindingId") bindingId: String,
    @Body request: Models.ChangeBindingActivityRequest
  ): Response<Unit>

  @DELETE("binding/{bindingId}")
  suspend fun deleteBinding(
    @Path("bindingId") bindingId: String
  ): Response<Unit>

  object Models {
    @Serializable
    data class GetBindingListResponse(
      @SerialName("data") val data: List<BindingInList>,
      @SerialName("pageNumber") val pageNumber: Int,
      @SerialName("pageSize") val pageSize: Int,
      @SerialName("totalPages") val totalPages: Int,
      @SerialName("totalCount") val totalCount: Int
    )

    @Serializable
    data class ChangeBindingActivityRequest(
      @SerialName("activity") val activity: Boolean
    )

    @Serializable
    data class BindingInList(
      @SerialName("id") val id: String,
      @SerialName("payerId") val payerId: String? = null,
      @SerialName("creationDate") val creationDate: String,
      @SerialName("lastUseDate") val lastUseDate: String,
      @SerialName("activity") val activity: Boolean,
      @SerialName("cardData") val cardData: CardData,
      @SerialName("product") val product: CardProduct? = null
    )

    @Serializable
    data class CardData(
      @SerialName("maskedPan") val maskedPan: String,
      @SerialName("expiryDate") val expiryDate: String,
      @SerialName("cardScheme") val cardScheme: String
    )

    @Serializable
    data class CardProduct(
      @SerialName("id") val id: String? = null,
      @SerialName("brand") val brand: String? = null,
      @SerialName("category") val category: String? = null
    )
  }
}