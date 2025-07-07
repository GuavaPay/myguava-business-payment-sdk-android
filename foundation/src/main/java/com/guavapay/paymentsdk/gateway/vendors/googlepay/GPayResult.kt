package com.guavapay.paymentsdk.gateway.vendors.googlepay

internal sealed class GPayResult {
  data class Success(val data: String) : GPayResult()
  data class Failed(val throwable: Throwable?) : GPayResult()
  object Canceled : GPayResult()
}