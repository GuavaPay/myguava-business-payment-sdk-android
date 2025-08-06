package com.guavapay.paymentsdk.logging

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.util.Log

internal fun i(msg: String) = i(tag(), msg)
internal fun d(msg: String) = d(tag(), msg)
internal fun v(msg: String) = v(tag(), msg)
internal fun w(msg: String) = w(tag(), msg)
internal fun e(msg: String) = e(tag(), msg)
internal fun e(msg: String, t: Throwable) = e(tag(), msg, t)

internal inline fun i(tag: String, msg: String) = Log.i(tag, msg).let {}
internal inline fun d(tag: String, msg: String) = Log.d(tag, msg).let {}
internal inline fun v(tag: String, msg: String) = Log.v(tag, msg).let {}
internal inline fun w(tag: String, msg: String) = Log.w(tag, msg).let {}
internal inline fun e(tag: String, msg: String) = Log.e(tag, msg).let {}
internal inline fun e(tag: String, msg: String, t: Throwable) = Log.e(tag, msg, t).let {}

private const val ANDROID_MAX_TAG = 23
private const val PREFIX = "PaymentSdk:"

private fun tag(): String {
  val base = inferCallerSimpleName()
  val maxClassLen = if (SDK_INT >= O) Int.MAX_VALUE else (ANDROID_MAX_TAG - PREFIX.length).coerceAtLeast(0)
  val short = ellipsize(base, maxClassLen)
  return PREFIX + short
}

private fun inferCallerSimpleName(): String {
  val st = Throwable().stackTrace
  for (el in st) {
    val cn = el.className
    if (shouldSkip(cn)) continue
    return simplifyClassName(cn)
  }
  return "Lib"
}

private fun shouldSkip(className: String) =
  className.startsWith("java.") ||
  className.startsWith("android.") ||
  className.startsWith("kotlin.") ||
  className.startsWith("kotlinx.") ||
  className.startsWith("androidx.compose.") ||
  className.startsWith("androidx.activity.compose.") ||
  className.startsWith("androidx.lifecycle.") ||
  className.startsWith("com.guavapay.paymentsdk.logging.") ||
  className.contains("ComposableSingletons") ||
  className.contains("\$ExternalSynthetic")

private fun simplifyClassName(className: String): String {
  val simple = className.substringAfterLast('.')
  val noDollar = simple.substringBefore('$')
  return noDollar.removeSuffix("Kt")
}

private fun ellipsize(s: String, maxLen: Int, dots: String = "..."): String {
  if (maxLen <= 0) return ""
  if (s.length <= maxLen) return s
  if (maxLen <= dots.length) return s.take(maxLen)
  val keep = maxLen - dots.length
  return s.take(keep) + dots
}
