package com.guavapay.paymentsdk.presentation.platform

import android.content.Context
import androidx.core.os.ConfigurationCompat.getLocales
import java.util.Locale

internal fun Context.locale() = getLocales(resources.configuration)[0] ?: Locale.getDefault() /* fallback value, never must happen. */