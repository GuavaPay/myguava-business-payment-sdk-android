package com.guavapay.paymentsdk.presentation.components.atomic

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.guavapay.paymentsdk.presentation.platform.LocalTokensProvider

@Composable internal fun SwitchButton(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier, enabled: Boolean = true) {
  val tokens = LocalTokensProvider.current
  Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier, enabled = enabled, colors = tokens.switch())
}