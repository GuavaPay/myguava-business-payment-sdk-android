package com.guavapay.paymentsdk.presentation.components.atoms

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.presentation.platform.LocalTokensProvider
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import androidx.compose.material3.RadioButton as Material3RadioButton

@Composable internal fun Radio(
  selected: Boolean,
  onClick: (() -> Unit)?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  colors: RadioButtonColors = LocalTokensProvider.current.radio(),
  interactionSource: MutableInteractionSource? = null
) {
  CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
    Material3RadioButton(selected = selected, onClick = onClick, modifier = modifier, enabled = enabled, colors = colors, interactionSource = interactionSource)
  }
}

private class RadioPreviewProvider : PreviewParameterProvider<Boolean> {
  override val values = sequenceOf(true, false)
}

@PreviewLightDark @Composable private fun CheckboxPreview(@PreviewParameter(RadioPreviewProvider::class) isChecked: Boolean) {
  PreviewTheme {
    Radio(selected = isChecked, onClick = null)
  }
}