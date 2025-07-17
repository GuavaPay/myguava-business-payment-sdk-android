package com.guavapay.paymentsdk.presentation.screens.mainpage

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.gateway.banking.PaymentKind
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayCoroutineScope
import com.guavapay.paymentsdk.gateway.vendors.googlepay.rememberGPayOrchestrator
import com.guavapay.paymentsdk.presentation.components.atomic.CircularProgressIndicator
import com.guavapay.paymentsdk.presentation.components.atomic.GooglePayButton
import com.guavapay.paymentsdk.presentation.components.atomic.PrimaryButton
import com.guavapay.paymentsdk.presentation.components.compound.CheckBoxCompound
import com.guavapay.paymentsdk.presentation.components.compound.PaymentNetworksIcons
import com.guavapay.paymentsdk.presentation.components.compound.TextFieldCompound
import com.guavapay.paymentsdk.presentation.looknfeel.threeds.threedslaf
import com.guavapay.paymentsdk.presentation.navigation.Route
import com.guavapay.paymentsdk.presentation.navigation.Route.AbortRoute
import com.guavapay.paymentsdk.presentation.navigation.Route.HomeRoute
import com.guavapay.paymentsdk.presentation.navigation.rememberNavBackStack
import com.guavapay.paymentsdk.presentation.platform.CardNumberVisualTransformation
import com.guavapay.paymentsdk.presentation.platform.CvcVisualTransformation
import com.guavapay.paymentsdk.presentation.platform.ExpiryDateVisualTransformation
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import com.guavapay.paymentsdk.presentation.platform.Text
import com.guavapay.paymentsdk.presentation.platform.ViewModelFactory
import com.guavapay.paymentsdk.presentation.platform.string
import com.guavapay.paymentsdk.presentation.screens.Screen
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainScreen.Actions
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.AbortDueConditions
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.AbortDueError
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.ChallengeRequired
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.FinishPayment
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.FocusCvv
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.FocusExp
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.FocusPan
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.HideKeyboard
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.PaymentError
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.RequiredContacts
import com.guavapay.paymentsdk.rememberLibraryUnit
import com.guavapay.myguava.business.myguava3ds2.transaction.ChallengeContract
import com.guavapay.myguava.business.myguava3ds2.transaction.ChallengeParameters
import com.guavapay.myguava.business.myguava3ds2.transaction.ChallengeResult
import com.guavapay.myguava.business.myguava3ds2.transaction.InitChallengeResult
import com.guavapay.myguava.business.myguava3ds2.transaction.Transaction
import kotlinx.coroutines.launch
import java.io.Serializable

internal object MainScreen : Screen<HomeRoute, Actions> {
  data class Actions(val finish: (PaymentResult) -> Unit = @JvmSerializableLambda {}) : Serializable

  @Composable override operator fun invoke(nav: SnapshotStateList<Route>, route: HomeRoute, actions: Actions) {
    val lib = rememberLibraryUnit()
    val vm = viewModel<MainVM>(factory = ViewModelFactory { MainVM(lib) })
    val state by vm.state.collectAsState()

    val panFocusRequester = remember(::FocusRequester)
    val expFocusRequester = remember(::FocusRequester)
    val cvvFocusRequester = remember(::FocusRequester)
    val keyboardController = LocalSoftwareKeyboardController.current

    val challengeLauncher = rememberLauncherForActivityResult(ChallengeContract()) { challengeResult ->
      when (challengeResult) {
        is ChallengeResult.Canceled -> vm.handles.unbusy()
        is ChallengeResult.Failed, is ChallengeResult.ProtocolError, is ChallengeResult.RuntimeError, is ChallengeResult.Timeout -> nav.add(AbortRoute())
        is ChallengeResult.Succeeded -> {}
      }
    }

    val threedslaf = threedslaf(lib.state.payload().threedsLooknfeel())
    LaunchedEffect(threedslaf) {
      vm.uiCustomization = threedslaf
    }

    LaunchedEffect(vm) {
      fun launchChallenge(challengeParams: ChallengeParameters, transaction: Transaction) {
        vm.handles.prepareChallengeConfig(challengeParams)

        val challengeRepositoryFactory = vm.handles.getChallengeRepositoryFactory(sdkTransactionId = transaction.sdkTransactionId, uiCustomization = threedslaf)
        PaymentGatewayCoroutineScope().launch {
          val startChallenge = challengeRepositoryFactory.startChallenge(transaction.createInitChallengeArgs(challengeParams, 10))
          if (startChallenge is InitChallengeResult.Start) {
            challengeLauncher.launch(startChallenge.challengeViewArgs)
          } else {
            nav.add(AbortRoute()) // todo: 3dsException.
          }
        }
      }

      vm.effects.collect { effect ->
        when (effect) {
          is RequiredContacts -> { /* Navigate onto ContactScreen */ }
          is AbortDueError -> nav.removeLastOrNull().also { nav.add(AbortRoute(effect.throwable)) }
          is PaymentError -> { /* TODO() */ }
          is AbortDueConditions -> { /* TODO() */ }
          is FinishPayment -> actions.finish(effect.result)
          is FocusPan -> panFocusRequester.requestFocus()
          is FocusExp -> expFocusRequester.requestFocus()
          is FocusCvv -> cvvFocusRequester.requestFocus()
          is HideKeyboard -> keyboardController?.hide()
          is ChallengeRequired -> launchChallenge(challengeParams = effect.challengeParameters, transaction = effect.transaction)
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

      if (state.external.busy) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        return@Column
      }

      if (state.external.gpay?.available == true && state.internal.orderData != null && state.internal.googlePayContext != null) {
        val orchestrator = rememberGPayOrchestrator(
          order = state.internal.orderData!!,
          gpayctx = state.internal.googlePayContext!!
        )
        GooglePayButton(orchestrator = orchestrator, result = vm.handles::gpay)
        Spacer(modifier = Modifier.height(16.dp))
        OrPayByCardDivider(title = stringResource(R.string.initial_or_pay_by_card))
        Spacer(modifier = Modifier.height(16.dp))
      }

      TextFieldCompound(
        modifier = Modifier.focusRequester(panFocusRequester),
        header = stringResource(R.string.initial_newcard_number),
        value = state.external.fields.pan,
        onValueChange = vm.handles::pan,
        onFocusLost = vm.handles::panFocusLost,
        placeholder = stringResource(R.string.initial_newcard_number_placeholder),
        loading = state.external.fields.panBusy,
        endIcon = state.external.fields.panNetwork?.image?.let { painterResource(it) },
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
        saveCardEnabled = state.external.saved?.available == true,
        onDone = vm.handles::pay,
        expFocusRequester = expFocusRequester,
        cvvFocusRequester = cvvFocusRequester
      )

      if (state.external.saved?.available == true) {
        Spacer(modifier = Modifier.height(24.dp))

        SaveCardBlock(
          checked = state.external.saving,
          onCheckedChange = vm.handles::toggleSave,
          cardName = state.external.fields.cn,
          onCardNameChange = vm.handles::cn,
          onCardNameFocusLost = vm.handles::cnFocusLost,
          cardNameError = state.external.fields.cnError,
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
    state: MainVM.State,
    onExpirationDateChange: (String) -> Unit,
    onExpirationDateFocusLost: () -> Unit,
    securityCode: String,
    onSecurityCodeChange: (String) -> Unit,
    onSecurityCodeFocusLost: () -> Unit,
    maxCvcLength: Int,
    saveCardEnabled: Boolean,
    onDone: (() -> Unit)? = null,
    expFocusRequester: FocusRequester,
    cvvFocusRequester: FocusRequester,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      TextFieldCompound(
        modifier = Modifier
          .weight(1f)
          .focusRequester(expFocusRequester),
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
        modifier = Modifier
          .weight(1f)
          .focusRequester(cvvFocusRequester),
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
    cardNameError: Text?,
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
            error = cardNameError?.string(),
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
        val text = remember(amount, kind) {
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

  private fun readResolve(): Any = MainScreen
}

@PreviewLightDark @Composable private fun PaymentGatewayMainPagePreview() {
  PreviewTheme { MainScreen(rememberNavBackStack(), HomeRoute, Actions()) }
}
