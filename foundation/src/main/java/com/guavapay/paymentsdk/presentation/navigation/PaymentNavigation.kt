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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.gateway.banking.PaymentResult.Error
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.WINDOW_ANIMATION_DURATION
import com.guavapay.paymentsdk.presentation.platform.NoActions
import com.guavapay.paymentsdk.presentation.screens.abort.AbortScreen
import com.guavapay.paymentsdk.presentation.screens.cancel.CancelScreen
import com.guavapay.paymentsdk.presentation.screens.contact.ContactScreen
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainScreen
import com.guavapay.paymentsdk.presentation.screens.management.CardEditScreen
import com.guavapay.paymentsdk.presentation.screens.management.CardRemoveScreen
import com.guavapay.paymentsdk.presentation.screens.phone.PhoneScreen

internal object Navigation {
  data class Actions(val finish: (PaymentResult) -> Unit)

  @Composable operator fun invoke(nav: SnapshotStateList<Route>, dialogs: SnapshotStateList<Route>, actions: Actions) {
    NavDisplay(
      backStack = nav,
      entryDecorators = listOf(
        rememberSceneSetupNavEntryDecorator(),
        rememberLoggingNavEntryDecorator(),
        rememberMetricaNavEntryDecorator(),
        rememberSavedStateNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator()
      ),
      onBack = { nav.removeLastOrNull() },
      modifier = Modifier,
      entryProvider = entryProvider {
        entry<Route.HomeRoute> { MainScreen(nav, it, actions = MainScreen.Actions(finish = { actions.finish(it) }, showDialog = { route -> dialogs.add(route) })) }
        entry<Route.ContactRoute> { ContactScreen(nav, it, actions = NoActions) }
        entry<Route.PhoneRoute> { PhoneScreen(nav, it, actions = NoActions) }
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

  @Composable fun DialogContent(route: Route, nav: SnapshotStateList<Route>, dialogRoutes: SnapshotStateList<Route>, actions: Actions) {
    val kb = LocalSoftwareKeyboardController.current
    val close = { kb?.hide() ; dialogRoutes.removeLastOrNull(); Unit }

    when (route) {
      is Route.AbortRoute -> AbortScreen(nav, route, actions = AbortScreen.Actions(finish = { close() ; actions.finish(Error(it)) }, close = close))
      is Route.CardRemoveRoute -> CardRemoveScreen(nav, route, actions = CardRemoveScreen.Actions(close = close))
      is Route.CardEditRoute -> CardEditScreen(nav, route, actions = CardEditScreen.Actions(close = close))
      is Route.CancelRoute -> CancelScreen(nav, route, actions = CancelScreen.Actions(finish = { close() ; actions.finish(it) }, close = close))
      else -> {}
    }
  }
}