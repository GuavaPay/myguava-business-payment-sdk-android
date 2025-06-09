package com.guavapay.paymentsdk.platform.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable

internal typealias ComposeString = ComposeStringProvider

@Immutable fun interface ComposeStringProvider {
  @ReadOnlyComposable @Composable operator fun invoke(): String
}