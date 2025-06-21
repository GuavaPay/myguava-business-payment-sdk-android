package com.guavapay.paymentsdk.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.cache
import androidx.compose.runtime.currentComposer as composer

@Composable inline fun <T, K> remember(key1: K, crossinline calculation: @DisallowComposableCalls (K) -> T) = composer.cache(composer.changed(key1)) { calculation(key1) }
