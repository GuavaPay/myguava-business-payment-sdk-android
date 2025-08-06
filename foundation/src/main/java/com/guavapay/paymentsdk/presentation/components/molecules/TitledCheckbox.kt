package com.guavapay.paymentsdk.presentation.components.molecules

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.presentation.components.atoms.Checkbox
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme

@Composable internal fun TitledCheckbox(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)? = null, text: String, modifier: Modifier = Modifier, enabled: Boolean = true) {
  val source = remember(::MutableInteractionSource)

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable(interactionSource = source, indication = null) { onCheckedChange?.invoke(!checked) },
    verticalAlignment = Alignment.CenterVertically
  ) {
    Checkbox(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    Spacer(modifier = Modifier.width(8.dp))
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
  }
}

private class TitledCheckBoxDesc(val checked: Boolean, val title: String)

private class TitledCheckboxPreviewProvider : PreviewParameterProvider<TitledCheckBoxDesc> {
  override val values = sequenceOf(TitledCheckBoxDesc(true, "Ammend commit"), TitledCheckBoxDesc(false, "Amend commit"))
}

@PreviewLightDark @Composable private fun TitledCheckboxPreview(@PreviewParameter(TitledCheckboxPreviewProvider ::class) desc: TitledCheckBoxDesc) {
  PreviewTheme {
    TitledCheckbox(checked = desc.checked, onCheckedChange = null, text = desc.title)
  }
}