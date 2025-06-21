package com.guavapay.paymentsdk.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import java.io.Serializable

typealias ComposeString = ComposeStringProvider

@Immutable fun interface ComposeStringProvider : Serializable {
  @ReadOnlyComposable @Composable operator fun invoke(): String
}