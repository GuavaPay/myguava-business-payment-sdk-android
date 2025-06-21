package com.guavapay.paymentsdk.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.WINDOW_ANIMATION_DURATION
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainPageScreen

internal object Navigation {
  data class Actions(val finish: (PaymentResult) -> Unit)

  @Composable operator fun invoke(initial: Route = Route.HomeRoute, actions: Actions) {
    val backStack = remember { mutableStateListOf(initial) }

    NavDisplay(
      backStack = backStack,
      onBack = { backStack.removeLastOrNull() },
      modifier = Modifier,
      entryProvider = entryProvider {
        entry<Route.HomeRoute> { MainPageScreen(actions = MainPageScreen.Actions(finish = actions.finish)) }
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