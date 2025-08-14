@file:Suppress("LocalVariableName", "RemoveRedundantQualifierName", "KotlinConstantConditions")
@file:OptIn(ExperimentalComposeUiApi::class)

package com.guavapay.paymentsdk.presentation.screens.mainpage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guavapay.myguava.business.myguava3ds2.transaction.ChallengeContract
import com.guavapay.myguava.business.myguava3ds2.transaction.ChallengeParameters
import com.guavapay.myguava.business.myguava3ds2.transaction.ChallengeResult
import com.guavapay.myguava.business.myguava3ds2.transaction.InitChallengeResult
import com.guavapay.myguava.business.myguava3ds2.transaction.Transaction
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.gateway.banking.PaymentKind
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayCoroutineScope
import com.guavapay.paymentsdk.gateway.vendors.googlepay.rememberGPayOrchestrator
import com.guavapay.paymentsdk.presentation.components.atoms.Progress
import com.guavapay.paymentsdk.presentation.components.molecules.GPayButton
import com.guavapay.paymentsdk.presentation.looknfeel.threeds.threedslaf
import com.guavapay.paymentsdk.presentation.navigation.Route
import com.guavapay.paymentsdk.presentation.navigation.Route.AbortRoute
import com.guavapay.paymentsdk.presentation.navigation.Route.CardEditRoute
import com.guavapay.paymentsdk.presentation.navigation.Route.CardRemoveRoute
import com.guavapay.paymentsdk.presentation.navigation.Route.ContactRoute
import com.guavapay.paymentsdk.presentation.navigation.Route.HomeRoute
import com.guavapay.paymentsdk.presentation.platform.rememberViewModel
import com.guavapay.paymentsdk.presentation.platform.string
import com.guavapay.paymentsdk.presentation.screens.Screen
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainScreen.Actions
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.AbortError
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.AbortGuard
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.AskContacts
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.ConfirmDeleteCard
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.EditCard
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.Finish
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.FocusCardholder
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.FocusCvv
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.FocusExp
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.FocusPan
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.HideKeyboard
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.Require3ds
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.Effect.ShowError
import com.guavapay.paymentsdk.presentation.screens.mainpage._components.InstrumentsDivider
import com.guavapay.paymentsdk.presentation.screens.mainpage._components.ModeSelector
import com.guavapay.paymentsdk.presentation.screens.mainpage._components.PayButton
import com.guavapay.paymentsdk.presentation.screens.mainpage._subsections.newcard.NewCardSection
import com.guavapay.paymentsdk.presentation.screens.mainpage._subsections.savedcard.SavedCardSection
import com.guavapay.paymentsdk.rememberLibraryUnit
import io.sentry.compose.SentryModifier.sentryTag
import io.sentry.compose.SentryTraced
import kotlinx.coroutines.launch
import java.io.Serializable

internal object MainScreen : Screen<HomeRoute, Actions> {
  data class Actions(
    val finish: (PaymentResult) -> Unit = @JvmSerializableLambda {},
    val showDialog: (Route) -> Unit = @JvmSerializableLambda {}
  ) : Serializable

  @Composable override operator fun invoke(nav: SnapshotStateList<Route>, route: HomeRoute, actions: Actions) = SentryTraced("main-screen") {
    val lib = rememberLibraryUnit()
    val vm = rememberViewModel(::MainVM)
    val state by vm.state.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()

    val panReq = remember(::FocusRequester)
    val expReq = remember(::FocusRequester)
    val cvvReq = remember(::FocusRequester)
    val chReq = remember(::FocusRequester)
    val kb = LocalSoftwareKeyboardController.current

    val launcher = rememberLauncherForActivityResult(ChallengeContract()) { res ->
      when (res) {
        is ChallengeResult.Canceled -> vm.handles.unbusy()
        is ChallengeResult.Failed,
        is ChallengeResult.ProtocolError,
        is ChallengeResult.RuntimeError,
        is ChallengeResult.Timeout -> actions.showDialog(AbortRoute())
        is ChallengeResult.Succeeded -> Unit
      }
    }

    val ui = threedslaf(lib.state.payload().threedsLooknfeel())
    LaunchedEffect(ui) { vm.uiCustomization = ui }

    LaunchedEffect(vm) {
      fun launchChallenge(params: ChallengeParameters, tx: Transaction) {
        vm.handles.prepareChallengeConfig(params)
        val repo = vm.handles.getChallengeRepositoryFactory(sdkTransactionId = tx.sdkTransactionId, uiCustomization = ui)
        PaymentGatewayCoroutineScope().launch {
          val init = repo.startChallenge(tx.createInitChallengeArgs(params, 10))
          if (init is InitChallengeResult.Start) launcher.launch(init.challengeViewArgs) else actions.showDialog(AbortRoute())
        }
      }

      vm.effects.collect { effect ->
        when (effect) {
          is AskContacts -> nav.add(ContactRoute(effect.countryIso, effect.callback))
          is AbortError -> actions.showDialog(AbortRoute(effect.throwable))
          is ShowError -> Unit
          is AbortGuard -> Unit
          is ConfirmDeleteCard -> actions.showDialog(CardRemoveRoute(effect.cardId, effect.cardName, effect.onDeleteConfirmed))
          is EditCard -> actions.showDialog(CardEditRoute(effect.cardId, effect.cardName, effect.onEditConfirmed))
          is Finish -> actions.finish(effect.result)
          is FocusPan -> panReq.requestFocus()
          is FocusExp -> expReq.requestFocus()
          is FocusCvv -> cvvReq.requestFocus()
          is FocusCardholder -> chReq.requestFocus()
          is HideKeyboard -> kb?.hide()
          is Require3ds -> launchChallenge(effect.params, effect.tx)
        }
      }
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(scroll)
        .padding(16.dp)
        .navigationBarsPadding()
        .imePadding()
    ) {
      Box(
        modifier = Modifier
          .width(40.dp)
          .height(4.dp)
          .background(MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraSmall)
          .align(Alignment.CenterHorizontally)
      )

      Spacer(Modifier.height(20.dp))

      if (state.busy) {
        Progress(Modifier.align(Alignment.CenterHorizontally))
        return@Column
      }

      val gpayxtras = remember(vm.internal.order, vm.internal.gpayCtx) { vm.internal.order to vm.internal.gpayCtx }
      if (state.gpay?.available == true && gpayxtras.first != null && gpayxtras.second != null) {
        val orchestrator = rememberGPayOrchestrator(order = gpayxtras.first!!, gpayctx = gpayxtras.second!!)
        GPayButton(
            modifier = Modifier.sentryTag("gpay-button"),
            orchestrator = orchestrator,
            result = vm.handles::gpay
        )
        Spacer(Modifier.height(16.dp))
        InstrumentsDivider(title = stringResource(R.string.initial_or_pay_by_card))
        Spacer(Modifier.height(16.dp))
      }

      val NewCardSection = @Composable { NewCardSection(
        state = state,
        panFocus = panReq,
        expFocus = expReq,
        cvvFocus = cvvReq,
        chFocus = chReq,
        actions = NewCardSection.Actions(
          onPan = vm.handles::pan,
          onPanBlur = vm.handles::panFocusLost,
          onExp = vm.handles::exp,
          onExpBlur = vm.handles::expFocusLost,
          onCvv = vm.handles::cvv,
          onCvvBlur = vm.handles::cvvFocusLost,
          onPay = vm.handles::pay,
          onCh = vm.handles::ch,
          onChBlur = vm.handles::chFocusLost,
          onToggleSave = vm.handles::toggleSave,
          onCn = vm.handles::cn,
          onCnBlur = vm.handles::cnFocusLost,
          onChangeContact = vm.handles::changeContactInfo
        )
      ) }

      if (state.saved?.available == true && state.saved?.cards?.isNotEmpty() == true) {
        val saved = state.saved!!

        ModeSelector(
          modifier = Modifier.sentryTag("mode-selector"),
          showSavedCards = state.mode == MainVM.State.Mode.SavedCard,
          onModeChanged = { show ->
            vm.handles.setPaymentScreenMode(if (show) MainVM.State.Mode.SavedCard else MainVM.State.Mode.NewCard)
          }
        )

        Spacer(Modifier.height(16.dp))

        Box {
          androidx.compose.animation.AnimatedVisibility(
            visible = state.mode == MainVM.State.Mode.SavedCard,
            enter = slideInHorizontally(tween(300)) { -it },
            exit = slideOutHorizontally(tween(300)) { -it }
          ) {
            SavedCardSection(
              cards = saved.cards,
              selectedCardId = saved.selectedCardId,
              cvvInput = saved.cvvInput,
              isLoading = saved.isLoadingCards,
              actions = SavedCardSection.Actions(
                onSelect = vm.handles::selectSavedCard,
                onCvvChange = vm.handles::savedCardCvv,
                onCvvDone = vm.handles::savedCardCvvFocusLost,
                onDelete = vm.handles::deleteCard,
                onEdit = vm.handles::editCard
              )
            )
          }

          androidx.compose.animation.AnimatedVisibility(
            visible = state.mode == MainVM.State.Mode.NewCard,
            enter = slideInHorizontally(tween(300)) { it },
            exit = slideOutHorizontally(tween(300)) { it }
          ) {
            NewCardSection()
          }
        }

        Spacer(Modifier.height(16.dp))
      } else {
        NewCardSection()
        Spacer(Modifier.height(16.dp))
      }

      Spacer(Modifier.height(24.dp))

      PayButton(
        modifier = Modifier.fillMaxWidth().sentryTag("pay-button"),
        amount = state.payText?.string(),
        enabled = vm.handles.isEligibleToPay,
        isLoading = state.busy,
        kind = state.payKind ?: PaymentKind.Pay,
        onClick = {
          if (state.mode == MainVM.State.Mode.NewCard) vm.handles.pay() else vm.handles.payViaSavedCard()
        }
      )
    }
  }

  private fun readResolve(): Any = MainScreen
}