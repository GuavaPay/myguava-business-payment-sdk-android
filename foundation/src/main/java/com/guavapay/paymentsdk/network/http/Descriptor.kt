package com.guavapay.paymentsdk.network.http

import okhttp3.Protocol
import retrofit2.Response

internal fun Response<*>.httpStatusLineDescriptor(): String {
  val proto = when (raw().protocol) {
    Protocol.HTTP_1_0 -> "HTTP/1.0"
    Protocol.HTTP_1_1 -> "HTTP/1.1"
    Protocol.HTTP_2, Protocol.H2_PRIOR_KNOWLEDGE -> "HTTP/2"
    Protocol.QUIC -> "HTTP/3"
    else -> "HTTP"
  }
  val code = code()
  val reason = message().takeIf(String::isNotBlank) ?: code.httpCodeDescriptor()
  return "$proto $code: $reason"
}

internal fun Response<*>.httpRequestDescriptor(): String {
  val req = raw().request
  val baseUrl = req.url.newBuilder().query(null).fragment(null).build().toString()
  return "${req.method} $baseUrl"
}

internal fun Response<*>.httpFullDescriptor() = "${httpRequestDescriptor()} -> ${httpStatusLineDescriptor()}"

internal fun Int.httpCodeDescriptor() = when (this) {
  400 -> "Bad Request"
  401 -> "Unauthorized"
  402 -> "Payment Required"
  403 -> "Forbidden"
  404 -> "Not Found"
  405 -> "Method Not Allowed"
  406 -> "Not Acceptable"
  408 -> "Request Timeout"
  409 -> "Conflict"
  410 -> "Gone"
  411 -> "Length Required"
  413 -> "Payload Too Large"
  414 -> "URI Too Long"
  415 -> "Unsupported Media Type"
  418 -> "I'm a teapot"
  422 -> "Unprocessable Entity"
  425 -> "Too Early"
  426 -> "Upgrade Required"
  429 -> "Too Many Requests"
  431 -> "Request Header Fields Too Large"
  451 -> "Unavailable For Legal Reasons"
  500 -> "Internal Server Error"
  501 -> "Not Implemented"
  502 -> "Bad Gateway"
  503 -> "Service Unavailable"
  504 -> "Gateway Timeout"
  505 -> "HTTP Version Not Supported"
  else -> "Not mapped :X"
}