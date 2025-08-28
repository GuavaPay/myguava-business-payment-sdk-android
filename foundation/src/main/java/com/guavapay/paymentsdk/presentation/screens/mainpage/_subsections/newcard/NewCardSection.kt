package com.guavapay.paymentsdk.presentation.screens.mainpage._subsections.newcard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.presentation.components.atoms.TextField
import com.guavapay.paymentsdk.presentation.components.sections.NetworksRow
import com.guavapay.paymentsdk.presentation.platform.CardNumberVisualTransformation
import com.guavapay.paymentsdk.presentation.platform.LocalParentScrollState
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import com.guavapay.paymentsdk.presentation.platform.ime
import com.guavapay.paymentsdk.presentation.platform.string
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM
import com.guavapay.paymentsdk.presentation.screens.mainpage._subsections.newcard._components.CardInputsBlock
import com.guavapay.paymentsdk.presentation.screens.mainpage._subsections.newcard._components.ContactInfoBlock
import com.guavapay.paymentsdk.presentation.screens.mainpage._subsections.newcard._components.SaveCardBlock
import com.guavapay.paymentsdk.rememberLibraryUnit
import io.sentry.compose.SentryModifier.sentryTag

internal object NewCardSection {
  data class Actions(
    val onPan: (String) -> Unit = {},
    val onPanBlur: () -> Unit = {},
    val onExp: (String) -> Unit = {},
    val onExpBlur: () -> Unit = {},
    val onCvv: (String) -> Unit = {},
    val onCvvBlur: () -> Unit = {},
    val onPay: () -> Unit = {},
    val onCh: (String) -> Unit = {},
    val onChBlur: () -> Unit = {},
    val onToggleSave: (Boolean) -> Unit = {},
    val onCn: (String) -> Unit = {},
    val onCnBlur: () -> Unit = {},
    val onChangeContact: () -> Unit = {}
  )

  @Composable operator fun invoke(
    state: MainVM.State,
    panFocus: FocusRequester,
    expFocus: FocusRequester,
    cvvFocus: FocusRequester,
    chFocus: FocusRequester,
    actions: Actions,
    modifier: Modifier = Modifier
  ) {
    val parent = LocalParentScrollState.current

    LaunchedEffect("scroll") {
      parent.animateScrollTo(0)
    }

    Column(modifier = modifier.fillMaxWidth()) {
      TextField(
        modifier = Modifier.focusRequester(panFocus).sentryTag("pan-input").ime(parent),
        header = stringResource(R.string.initial_newcard_number),
        value = state.fields.pan,
        onValueChange = actions.onPan,
        onFocusLost = actions.onPanBlur,
        placeholder = stringResource(R.string.initial_newcard_number_placeholder),
        loading = state.fields.panBusy,
        endIcon = state.fields.panNetwork?.image?.let { painterResource(it) },
        endIconTint = Color.Unspecified,
        error = state.fields.panError?.string(),
        singleLine = true,
        maxLength = 19,
        ignorable = " ",
        visualTransformation = CardNumberVisualTransformation(),
      )

      Spacer(Modifier.height(12.dp))

      CardInputsBlock(
        state = state,
        cvv = state.fields.cvv,
        maxCvc = state.fields.cvvLength,
        saveEnabled = state.saved?.savingAvailable == true,
        holderAvailable = state.fields.cardHolderNameAvailable,
        expFocus = expFocus,
        cvvFocus = cvvFocus,
        actions = CardInputsBlock.Actions(
          onExp = actions.onExp,
          onExpBlur = actions.onExpBlur,
          onCvv = actions.onCvv,
          onCvvBlur = actions.onCvvBlur,
          onDone = actions.onPay
        ),
      )

      if (state.fields.cardHolderNameAvailable) {
        Spacer(Modifier.height(12.dp))

        TextField(
          modifier = Modifier.focusRequester(chFocus).sentryTag("cardholder-input").ime(parent),
          header = stringResource(R.string.initial_newcard_cardholder_name),
          value = state.fields.ch,
          onValueChange = actions.onCh,
          onFocusLost = actions.onChBlur,
          placeholder = stringResource(R.string.initial_newcard_cardholder_name_placeholder),
          error = state.fields.chError?.string(),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = if (state.saved?.savingAvailable == true) ImeAction.Next else ImeAction.Done
          ),
          singleLine = true,
          maxLength = 25,
          onDoneAction = actions.onPay
        )
      }

      if (state.saved?.savingAvailable == true) {
        Spacer(Modifier.height(24.dp))

        SaveCardBlock(
          checked = state.saving,
          name = state.fields.cn,
          error = state.fields.cnError,
          actions = SaveCardBlock.Actions(
            onToggle = actions.onToggleSave,
            onName = actions.onCn,
            onNameBlur = actions.onCnBlur,
            onDone = actions.onPay
          ),
          modifier = Modifier.sentryTag("save-card-block")
        )
      }

      state.contact?.let { contact ->
        if (contact.maskedEmail.isNotEmpty() || contact.maskedPhone.isNotEmpty()) {
          Spacer(Modifier.height(24.dp))

          ContactInfoBlock(
            payload = ContactInfoBlock.Payload(
              email = contact.maskedEmail,
              phoneNumber = contact.maskedPhone
            ),
            onChangeInfoClick = actions.onChangeContact,
            modifier = Modifier.sentryTag("contact-info-block")
          )
        }
      }

      Spacer(Modifier.height(24.dp))

      NetworksRow(
        networks = state.networks,
        modifier = Modifier.align(Alignment.CenterHorizontally)
      )
    }
  }
}

@PreviewLightDark @Composable private fun NewCardSectionPreview() {
  PreviewTheme {
    val lib = rememberLibraryUnit()
    val focuses = List(4) { remember(::FocusRequester) }
    NewCardSection(state = MainVM.State(networks = lib.state.payload().availableCardSchemes.toList()), panFocus = focuses[0], expFocus = focuses[1], cvvFocus = focuses[2], chFocus = focuses[3], actions = NewCardSection.Actions())
  }
}