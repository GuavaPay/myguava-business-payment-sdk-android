package com.guavapay.paymentsdk.network.ssevents

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.sse.EventSources

internal class SseClient(client: OkHttpClient) {
  private val factory = EventSources.createFactory(client)

  fun events(request: Request) = callbackFlow {
    val source = factory.newEventSource(request, SseAdapter(this))
    awaitClose(source::cancel)
  }
}
