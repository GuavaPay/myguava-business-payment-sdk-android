package com.guavapay.paymentsdk.network.ssevents

internal sealed class SseEvent {
  data object Open : SseEvent()
  data object Closed : SseEvent()
  data class Failure(val code: Int?, val message: String?, val throwable: Throwable?) : SseEvent()
  data class Message(val id: String?, val type: String?, val data: String) : SseEvent()
}