package com.guavapay.paymentsdk.presentation.screens.mainpage._subsections.newcard._components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.presentation.components.atoms.TextField
import com.guavapay.paymentsdk.presentation.components.molecules.TitledCheckbox
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import com.guavapay.paymentsdk.presentation.platform.Text
import com.guavapay.paymentsdk.presentation.platform.string

internal object SaveCardBlock {
  data class Actions(
    val onToggle: (Boolean) -> Unit = {},
    val onName: (String) -> Unit = {},
    val onNameBlur: () -> Unit = {},
    val onDone: (() -> Unit)? = null
  )

  @Composable operator fun invoke(
    checked: Boolean,
    name: String,
    error: Text?,
    actions: Actions = Actions(),
    modifier: Modifier = Modifier,
  ) {
    Column(modifier = modifier.fillMaxWidth()) {
      TitledCheckbox(
        checked = checked,
        onCheckedChange = actions.onToggle,
        text = stringResource(R.string.initial_newcard_save)
      )

      AnimatedVisibility(
        visible = checked,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300), initialOffsetY = { -it / 2 }),
        exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(animationSpec = tween(300), targetOffsetY = { -it / 2 })
      ) {
        Column {
          Spacer(Modifier.height(24.dp))

          TextField(
            header = stringResource(R.string.initial_newcard_name),
            value = name,
            onValueChange = actions.onName,
            maxLength = 200,
            placeholder = stringResource(R.string.initial_newcard_name_placeholder),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
            onFocusLost = actions.onNameBlur,
            error = error?.string(),
            onDoneAction = actions.onDone
          )
        }
      }
    }
  }
}

private class SaveCardBlockPreviewProvider : PreviewParameterProvider<Boolean> {
  override val values = sequenceOf(true, false)
}

@PreviewLightDark @Composable private fun InstrumentsPreview(@PreviewParameter(SaveCardBlockPreviewProvider ::class) isChecked: Boolean) {
  PreviewTheme {
    SaveCardBlock(isChecked, "Pavel Erokhin's Card", null)
  }
}