package com.guavapay.paymentsdk.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import java.io.Serializable

/**
 * Represents a string that can be composed using a ComposeStringProvider.
 *
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
typealias ComposeString = ComposeStringProvider

/**
 * A functional interface that provides a string via Compose.
 *
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
@Immutable fun interface ComposeStringProvider : Serializable {
  @ReadOnlyComposable @Composable operator fun invoke(): String
}