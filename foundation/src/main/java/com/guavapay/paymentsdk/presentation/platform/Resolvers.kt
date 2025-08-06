package com.guavapay.paymentsdk.presentation.platform

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.ui.text.intl.Locale
import java.util.concurrent.ConcurrentHashMap

internal object FlagResolver {
  private val cache = ConcurrentHashMap<String, Int>()

  @DrawableRes fun resolve(context: Context, countryCode: String): Int {
    val key = countryCode.lowercase(Locale.current.platformLocale).replace("-", "_")
    return cache.getOrPut(key) { context.resources.getIdentifier("ic_flag_$key", "drawable", context.packageName) }
  }
}