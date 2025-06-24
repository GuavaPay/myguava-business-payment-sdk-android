package com.guavapay.paymentsdk.network.ssevents

internal sealed class SseEvent {
  data object Open : SseEvent()
  data class Message(val id: String?, val type: String?, val data: String) : SseEvent()
}