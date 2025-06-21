package com.guavapay.paymentsdk.presentation.components.atomic

import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Checkbox as Material3Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.presentation.platform.LocalSizesProvider
import com.guavapay.paymentsdk.presentation.platform.LocalTokensProvider

@Composable internal fun Checkbox(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier, enabled: Boolean = true) {
  val tokens = LocalTokensProvider.current
  val sizes = LocalSizesProvider.current

  CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
    Material3Checkbox(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier.height(sizes.checkbox().height), enabled = enabled, colors = tokens.checkbox())
  }
}