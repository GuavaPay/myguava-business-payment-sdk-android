package com.guavapay.paymentsdk.presentation.components.atoms

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.presentation.platform.LocalSizesProvider
import com.guavapay.paymentsdk.presentation.platform.LocalTokensProvider
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import com.guavapay.paymentsdk.presentation.platform.defaultMinSizeIfUnspecified
import androidx.compose.material3.Checkbox as Material3Checkbox

@Composable internal fun Checkbox(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier, enabled: Boolean = true, colors: CheckboxColors = LocalTokensProvider.current.checkbox(), interactionSource: MutableInteractionSource? = null) {
  val sizes = LocalSizesProvider.current

  CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
    Material3Checkbox(
      checked = checked,
      onCheckedChange = onCheckedChange,
      modifier = modifier.defaultMinSizeIfUnspecified(minHeight = sizes.checkbox().height),
      enabled = enabled,
      colors = colors,
      interactionSource = interactionSource,
    )
  }
}

private class CheckboxPreviewProvider : PreviewParameterProvider<Boolean> {
  override val values = sequenceOf(true, false)
}

@PreviewLightDark @Composable private fun CheckboxPreview(@PreviewParameter(CheckboxPreviewProvider ::class) isChecked: Boolean) {
  PreviewTheme {
    Checkbox(checked = isChecked, onCheckedChange = null)
  }
}