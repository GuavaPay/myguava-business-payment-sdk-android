package com.guavapay.paymentsdk.network.ssevents

internal class SseException(val code: Int = 999, message: String? = null, cause: Throwable? = null) : Exception("SSE HTTP Status Code: $code, message: $message", cause)