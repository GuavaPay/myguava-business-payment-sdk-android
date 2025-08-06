package com.guavapay.paymentsdk.presentation.screens.mainpage._components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.presentation.components.molecules.TabHost
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme

@Composable internal fun ModeSelector(showSavedCards: Boolean, onModeChanged: (Boolean) -> Unit, modifier: Modifier = Modifier) {
  TabHost(
    mode = if (showSavedCards) 0 else 1,
    changed = { mode -> onModeChanged(mode == 0) },
    tabs = listOf(
      TabHost.Node(stringResource(R.string.mode_selector_saved_cards)),
      TabHost.Node(stringResource(R.string.mode_selector_new_card))
    ),
    modifier = modifier
  )
}

private class ModeSelectorPreviewProvider : PreviewParameterProvider<Boolean> {
  override val values = sequenceOf(true, false)
}

@PreviewLightDark @Composable private fun InstrumentsPreview(@PreviewParameter(ModeSelectorPreviewProvider ::class) isChecked: Boolean) {
  PreviewTheme {
    ModeSelector(showSavedCards = isChecked, onModeChanged = {})
  }
}