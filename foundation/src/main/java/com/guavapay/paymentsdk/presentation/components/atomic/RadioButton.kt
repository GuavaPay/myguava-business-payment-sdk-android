package com.guavapay.paymentsdk.presentation.components.atomic

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.guavapay.paymentsdk.presentation.platform.LocalTokensProvider
import androidx.compose.material3.RadioButton as Material3RadioButton

@Composable internal fun RadioButton(selected: Boolean, onClick: (() -> Unit)?, modifier: Modifier = Modifier, enabled: Boolean = true) {
  val tokens = LocalTokensProvider.current
  Material3RadioButton(selected = selected, onClick = onClick, modifier = modifier, enabled = enabled, colors = tokens.radio())
}