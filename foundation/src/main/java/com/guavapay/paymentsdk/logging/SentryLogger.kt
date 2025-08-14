package com.guavapay.paymentsdk.logging

import io.sentry.ILogger
import io.sentry.SentryLevel
import io.sentry.SentryLevel.DEBUG
import io.sentry.SentryLevel.ERROR
import io.sentry.SentryLevel.FATAL
import io.sentry.SentryLevel.INFO
import io.sentry.SentryLevel.WARNING
import java.util.Locale

internal class SentryLogger(private val minLevel: SentryLevel = DEBUG) : ILogger {
  override fun log(level: SentryLevel, message: String, vararg args: Any?) = logInternal(level, null, message, *args)
  override fun log(level: SentryLevel, message: String, throwable: Throwable?) = logInternal(level, throwable, message)
  override fun log(level: SentryLevel, throwable: Throwable?, message: String, vararg args: Any?) = logInternal(level, throwable, message, *args)

  private fun logInternal(level: SentryLevel, throwable: Throwable?, message: String, vararg args: Any?) {
    if (!isEnabled(level)) return
    val formatted = format(message, args)

    when (level) {
      DEBUG -> d(formatted)
      INFO -> i(formatted)
      WARNING -> w(formatted)
      ERROR, FATAL -> if (throwable != null) e(formatted, throwable) else e(formatted)
    }
  }

  private fun format(message: String, args: Array<out Any?>): String =
    if (args.isEmpty()) message
    else try {
      String.format(Locale.ROOT, message, *args)
    } catch (e: Exception) {
      "$message (formatting failed: ${e.message})"
    }

  override fun isEnabled(level: SentryLevel?) = (level?.ordinal ?: Int.MIN_VALUE) >= minLevel.ordinal
}
