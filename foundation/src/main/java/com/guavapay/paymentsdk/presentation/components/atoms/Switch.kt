package com.guavapay.paymentsdk.presentation.components.atoms

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.presentation.platform.LocalTokensProvider
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme

@Composable internal fun Switch(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier, thumbContent: (@Composable () -> Unit)? = null, enabled: Boolean = true, colors: SwitchColors = LocalTokensProvider.current.switch(), interactionSource: MutableInteractionSource? = null) {
  CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
    Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier, thumbContent = thumbContent, enabled = enabled, colors = colors, interactionSource = interactionSource)
  }
}

private class SwitchPreviewProvider : PreviewParameterProvider<Boolean> {
  override val values = sequenceOf(true, false)
}

@PreviewLightDark @Composable private fun CheckboxPreview(@PreviewParameter(SwitchPreviewProvider ::class) isChecked: Boolean) {
  PreviewTheme {
    Switch(checked = isChecked, onCheckedChange = null)
  }
}