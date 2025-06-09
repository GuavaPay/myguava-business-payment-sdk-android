package com.guavapay.paymentsdk.logging

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.util.Log
import com.guavapay.paymentsdk.platform.function.ℓ
import com.guavapay.paymentsdk.platform.reflection.CallerFactory
import com.guavapay.paymentsdk.platform.reflection.CallerFactory.caller

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

private fun tag() = with(CallerFactory.Types.Class) { "PaymentSdk:${stripTag(caller(2).simpleName)}" }

private const val MaxTagLength = 19
private fun stripTag(classname: String, stippee: String = "..."): String {
  val tag = classname.substringAfterLast('.')
  return tag.takeIf { it.length <= MaxTagLength || SDK_INT >= O } ?: ℓ {
    val stripped = tag.take(MaxTagLength)
    val strippedLength = stripped.length
    stripped.replaceRange(strippedLength - stippee.length..<strippedLength, stippee)
  }
}
