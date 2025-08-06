package com.guavapay.paymentsdk.presentation.screens.mainpage._subsections.newcard._components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.presentation.components.atoms.TextField
import com.guavapay.paymentsdk.presentation.platform.ExpiryDateVisualTransformation
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import com.guavapay.paymentsdk.presentation.platform.rememberCvcPeekState
import com.guavapay.paymentsdk.presentation.platform.string
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM

internal object CardInputsBlock {
  data class Actions(
    val onExp: (String) -> Unit = {},
    val onExpBlur: () -> Unit = {},
    val onCvv: (String) -> Unit = {},
    val onCvvBlur: () -> Unit = {},
    val onDone: (() -> Unit)? = null
  )

  @Composable operator fun invoke(
    state: MainVM.State,
    cvv: String,
    maxCvc: Int,
    saveEnabled: Boolean,
    holderAvailable: Boolean,
    expFocus: FocusRequester,
    cvvFocus: FocusRequester,
    actions: Actions = Actions()
  ) {
    val ime = if (holderAvailable || saveEnabled) ImeAction.Next else ImeAction.Done

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      TextField(
        modifier = Modifier
          .weight(1f)
          .focusRequester(expFocus),
        value = state.fields.exp,
        onValueChange = actions.onExp,
        onFocusLost = actions.onExpBlur,
        header = stringResource(R.string.initial_newcard_expiration),
        placeholder = stringResource(R.string.initial_newcard_expiration_placeholder),
        error = state.fields.expError?.string(),
        singleLine = true,
        maxLength = 4,
        visualTransformation = ExpiryDateVisualTransformation(),
      )

      val transform = rememberCvcPeekState(maxLength = maxCvc)

      TextField(
        modifier = Modifier
          .weight(1f)
          .focusRequester(cvvFocus),
        header = stringResource(R.string.initial_newcard_cvv),
        value = cvv,
        onValueChange = { transform.onTextChanged(it) ; actions.onCvv(it) },
        onFocusLost = { transform.onFocusLost() ; actions.onCvvBlur() },
        placeholder = stringResource(R.string.initial_newcard_cvv_placeholder),
        error = state.fields.cvvError?.string(),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
          imeAction = ime
        ),
        singleLine = true,
        maxLength = maxCvc,
        visualTransformation = transform.visualTransformation(),
        onDoneAction = { transform.onFocusLost() ; actions.onDone?.invoke() },
      )
    }
  }
}

@PreviewLightDark @Composable private fun CardInputsBlockPreview() {
  PreviewTheme {
    CardInputsBlock(
      state = MainVM.State(),
      cvv = "340",
      maxCvc = 3,
      saveEnabled = false,
      holderAvailable = false,
      expFocus = FocusRequester(),
      cvvFocus = FocusRequester(),
    )
  }
}
