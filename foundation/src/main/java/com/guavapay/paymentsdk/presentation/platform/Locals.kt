package com.guavapay.paymentsdk.presentation.platform

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.staticCompositionLocalOf

inline fun <reified T> requiredStaticCompositionLocalOf() = staticCompositionLocalOf<T> { error("No ${T::class.simpleName} instances provided") }

internal val LocalParentScrollState = requiredStaticCompositionLocalOf<ScrollState>()