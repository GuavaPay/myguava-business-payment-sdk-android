package com.guavapay.paymentsdk.network.ssevents

import com.guavapay.paymentsdk.logging.d
import kotlinx.coroutines.channels.ProducerScope
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.Response
import okhttp3.internal.http2.StreamResetException
import java.net.SocketTimeoutException

internal class SseAdapter(private val producer: ProducerScope<SseEvent>) : EventSourceListener() {
  override fun onOpen(eventSource: EventSource, response: Response) {
    d("SSE event source opened onto ${eventSource.request().url}")
    producer.trySend(SseEvent.Open)
  }

  override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
    d("SSE event source received event: $id, $type, $data")
    producer.trySend(SseEvent.Message(id, type, data))
  }

  override fun onClosed(eventSource: EventSource) {
    d("SSE event source closed")
    producer.trySend(SseEvent.Closed)
    producer.channel.close()
  }

  override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
    if (t is StreamResetException || t is SocketTimeoutException) {
      onClosed(eventSource)
    } else {
      d("SSE event source failed: $t, $response")
      producer.trySend(SseEvent.Failure(response?.code, response?.message, t))
      producer.channel.close(t)
    }
  }
}