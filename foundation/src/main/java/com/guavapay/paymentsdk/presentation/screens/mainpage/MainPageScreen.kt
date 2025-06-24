package com.guavapay.paymentsdk.presentation.screens.mainpage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.gateway.banking.PaymentKind
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.gateway.launcher.LocalGatewayState
import com.guavapay.paymentsdk.presentation.components.atomic.CircularProgressIndicator
import com.guavapay.paymentsdk.presentation.components.atomic.GooglePayButton
import com.guavapay.paymentsdk.presentation.components.atomic.PrimaryButton
import com.guavapay.paymentsdk.presentation.components.compound.CheckBoxCompound
import com.guavapay.paymentsdk.presentation.components.compound.PaymentNetworksIcons
import com.guavapay.paymentsdk.presentation.components.compound.TextFieldCompound
import com.guavapay.paymentsdk.presentation.platform.CardNumberVisualTransformation
import com.guavapay.paymentsdk.presentation.platform.CvcVisualTransformation
import com.guavapay.paymentsdk.presentation.platform.ExpiryDateVisualTransformation
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import com.guavapay.paymentsdk.presentation.platform.ViewModelFactory
import com.guavapay.paymentsdk.presentation.platform.string
import com.guavapay.paymentsdk.rememberLibraryUnit

internal object MainPageScreen {
  data class Actions(val finish: (PaymentResult) -> Unit = {})

  @Composable operator fun invoke(actions: Actions) {
    val gs = LocalGatewayState.current
    val lib = rememberLibraryUnit()
    val vm = viewModel<MainPageVM>(factory = ViewModelFactory { MainPageVM(lib, gs) })
    val state by vm.state.collectAsState()

    LaunchedEffect(vm) {
      vm.effects.collect { effect ->
        when (effect) {
          is MainPageVM.Effect.RequiredContacts -> { /* Navigate onto ContactScreen */ }
          is MainPageVM.Effect.AbortDueError -> { /* Navigate to AbortScreen */ }
          is MainPageVM.Effect.PaymentError -> { /* TODO() */ }
          is MainPageVM.Effect.NavigateToResult -> {
            actions.finish(if (effect.success) PaymentResult.Completed else PaymentResult.Completed /* Mocked until not integrated with BE */)
          }
        }
      }
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(24.dp)
        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
        .imePadding()
    ) {
      Box(
        modifier = Modifier
          .width(40.dp)
          .height(4.dp)
          .background(
            MaterialTheme.colorScheme.outline,
            MaterialTheme.shapes.extraSmall
          )
          .align(Alignment.CenterHorizontally)
      )

      Spacer(modifier = Modifier.height(20.dp))

      if (state.external.flags.gpay) {
        val gpay = remember(gs.instruments.methods) { gs.instruments.instrument<PaymentMethod.GooglePay>() }

        if (gpay != null) {
          GooglePayButton(gs, vm.handles::gpay)
          Spacer(modifier = Modifier.height(16.dp))
          OrPayByCardDivider(title = stringResource(R.string.initial_or_pay_by_card))
          Spacer(modifier = Modifier.height(16.dp))
        }
      }

      TextFieldCompound(
        header = stringResource(R.string.initial_newcard_number),
        value = state.external.fields.pan,
        onValueChange = vm.handles::pan,
        onFocusLost = vm.handles::panFocusLost,
        placeholder = stringResource(R.string.initial_newcard_number_placeholder),
        loading = state.external.fields.panBusy,
        endIcon = state.external.fields.panNetwork?.imageres?.let { painterResource(it) },
        error = state.external.fields.panError?.string(),
        singleLine = true,
        maxLength = 19,
        visualTransformation = CardNumberVisualTransformation()
      )

      Spacer(modifier = Modifier.height(12.dp))

      CardFieldsGroup(
        state = state,
        onExpirationDateChange = vm.handles::exp,
        onExpirationDateFocusLost = vm.handles::expFocusLost,
        securityCode = state.external.fields.cvv,
        onSecurityCodeChange = vm.handles::cvv,
        onSecurityCodeFocusLost = vm.handles::cvvFocusLost,
        maxCvcLength = state.external.fields.cvvLength,
        saveCardEnabled = state.external.flags.save,
        onDone = vm.handles::pay
      )

      Spacer(modifier = Modifier.height(24.dp))

      if (state.external.flags.save) {
        SaveCardBlock(
          checked = state.external.saving,
          onCheckedChange = vm.handles::toggleSave,
          cardName = state.external.fields.cn,
          onCardNameChange = vm.handles::cn,
          onCardNameFocusLost = vm.handles::cnFocusLost,
          onDone = vm.handles::pay
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      PaymentNetworksIcons(networks = state.external.networks, modifier = Modifier.align(Alignment.CenterHorizontally))

      Spacer(modifier = Modifier.height(32.dp))

      PayButton(
        amount = state.external.paytext?.string(),
        enabled = vm.handles.isEligibleToPay,
        isLoading = state.external.busy,
        kind = state.external.paykind ?: PaymentKind.Pay,
        onClick = vm.handles::pay
      )
    }
  }

  @Composable private fun OrPayByCardDivider(title: String, modifier: Modifier = Modifier) {
    Row(
      modifier = modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      HorizontalDivider(
        modifier = Modifier.weight(1f),
        color = MaterialTheme.colorScheme.outlineVariant
      )

      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
      )

      HorizontalDivider(
        modifier = Modifier.weight(1f),
        color = MaterialTheme.colorScheme.outlineVariant
      )
    }
  }

  @Composable private fun CardFieldsGroup(
    state: MainPageVM.State,
    onExpirationDateChange: (String) -> Unit,
    onExpirationDateFocusLost: () -> Unit,
    securityCode: String,
    onSecurityCodeChange: (String) -> Unit,
    onSecurityCodeFocusLost: () -> Unit,
    maxCvcLength: Int,
    saveCardEnabled: Boolean,
    onDone: (() -> Unit)? = null,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      TextFieldCompound(
        modifier = Modifier.weight(1f),
        value = state.external.fields.exp,
        onValueChange = onExpirationDateChange,
        onFocusLost = onExpirationDateFocusLost,
        header = stringResource(R.string.initial_newcard_expiration),
        placeholder = stringResource(R.string.initial_newcard_expiration_placeholder),
        error = state.external.fields.expError?.string(),
        singleLine = true,
        maxLength = 4,
        visualTransformation = ExpiryDateVisualTransformation(),
      )

      TextFieldCompound(
        modifier = Modifier.weight(1f),
        header = stringResource(R.string.initial_newcard_cvv),
        value = securityCode,
        onValueChange = onSecurityCodeChange,
        onFocusLost = onSecurityCodeFocusLost,
        placeholder = stringResource(R.string.initial_newcard_cvv_placeholder),
        error = state.external.fields.cvvError?.string(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = if (saveCardEnabled) ImeAction.Next else ImeAction.Done),
        singleLine = true,
        maxLength = maxCvcLength,
        visualTransformation = CvcVisualTransformation(),
        onDoneAction = onDone,
      )
    }
  }

  @Composable private fun SaveCardBlock(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    cardName: String,
    onCardNameChange: (String) -> Unit,
    onCardNameFocusLost: () -> Unit,
    onDone: (() -> Unit)? = null,
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      CheckBoxCompound(
        checked = checked,
        onCheckedChange = onCheckedChange,
        text = stringResource(R.string.initial_newcard_save)
      )

      AnimatedVisibility(
        visible = checked,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
          animationSpec = tween(300),
          initialOffsetY = { -it / 2 }
        ),
        exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(
          animationSpec = tween(300),
          targetOffsetY = { -it / 2 }
        )
      ) {
        Column {
          Spacer(modifier = Modifier.height(24.dp))

          TextFieldCompound(
            header = stringResource(R.string.initial_newcard_name),
            value = cardName,
            onValueChange = onCardNameChange,
            placeholder = stringResource(R.string.initial_newcard_name_placeholder),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
            onFocusLost = onCardNameFocusLost,
            onDoneAction = onDone
          )
        }
      }
    }
  }

  @Composable private fun PayButton(amount: String?, kind: PaymentKind, enabled: Boolean, isLoading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    PrimaryButton(
      onClick = onClick,
      enabled = enabled,
      modifier = modifier
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          strokeWidth = 2.dp
        )
      } else {
        val buttonKind = kind.text()
        val text = remember (amount, kind) {
          buildString {
            append(buttonKind)
            if (amount != null) {
              append(" ")
              append(amount)
            }
          }
        }

        Text(
          text = text,
          style = MaterialTheme.typography.labelLarge,
        )
      }
    }
  }
}

@PreviewLightDark @Composable private fun PaymentGatewayMainPagePreview() {
  PreviewTheme { MainPageScreen(actions = MainPageScreen.Actions()) }
}
