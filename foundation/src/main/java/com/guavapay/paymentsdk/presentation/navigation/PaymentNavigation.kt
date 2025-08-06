@file:Suppress("NestedLambdaShadowedImplicitParameter")

package com.guavapay.paymentsdk.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.gateway.banking.PaymentResult.Error
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.WINDOW_ANIMATION_DURATION
import com.guavapay.paymentsdk.presentation.screens.abort.AbortScreen
import com.guavapay.paymentsdk.presentation.screens.cancel.CancelScreen
import com.guavapay.paymentsdk.presentation.screens.contact.ContactScreen
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainScreen
import com.guavapay.paymentsdk.presentation.screens.management.CardEditScreen
import com.guavapay.paymentsdk.presentation.screens.management.CardRemoveScreen
import com.guavapay.paymentsdk.presentation.screens.phone.PhoneScreen

internal object Navigation {
  data class Actions(val finish: (PaymentResult) -> Unit)

  @Composable operator fun invoke(nav: SnapshotStateList<Route>, actions: Actions) {
    NavDisplay(
      backStack = nav,
      entryDecorators = listOf(
        rememberSceneSetupNavEntryDecorator(),
        rememberLoggingNavEntryDecorator(),
        rememberSavedStateNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator()
      ),
      onBack = { nav.removeLastOrNull() },
      modifier = Modifier,
      entryProvider = entryProvider {
        entry<Route.HomeRoute> { MainScreen(nav, it, actions = MainScreen.Actions(finish = { actions.finish(it) })) }
        entry<Route.AbortRoute> { AbortScreen(nav, it, actions = AbortScreen.Actions(finish = { actions.finish(Error(it)) })) }
        entry<Route.CardRemoveRoute> { CardRemoveScreen(nav, it, actions = CardRemoveScreen.Actions) }
        entry<Route.CardEditRoute> { CardEditScreen(nav, it, actions = CardEditScreen.Actions) }
        entry<Route.CancelRoute> { CancelScreen(nav, it, actions = CancelScreen.Actions(finish = { actions.finish(it) })) }
        entry<Route.ContactRoute> { ContactScreen(nav, it, actions = ContactScreen.Actions) }
        entry<Route.PhoneRoute> { PhoneScreen(nav, it, actions = PhoneScreen.Actions) }
      },
      transitionSpec = {
        slideInHorizontally(
          initialOffsetX = { it },
          animationSpec = tween(durationMillis = WINDOW_ANIMATION_DURATION)
        ) + fadeIn(animationSpec = tween(durationMillis = WINDOW_ANIMATION_DURATION)) togetherWith slideOutHorizontally(
          targetOffsetX = { -it },
          animationSpec = tween(durationMillis = WINDOW_ANIMATION_DURATION)
        ) + fadeOut(animationSpec = tween(durationMillis = WINDOW_ANIMATION_DURATION))
      },
      popTransitionSpec = {
        slideInHorizontally(
          initialOffsetX = { -it },
          animationSpec = tween(durationMillis = WINDOW_ANIMATION_DURATION)
        ) + fadeIn(animationSpec = tween(durationMillis = WINDOW_ANIMATION_DURATION)) togetherWith slideOutHorizontally(
          targetOffsetX = { it },
          animationSpec = tween(durationMillis = WINDOW_ANIMATION_DURATION)
        ) + fadeOut(animationSpec = tween(durationMillis = WINDOW_ANIMATION_DURATION))
      }
    )
  }
}