@file:OptIn(ExperimentalLayoutApi::class)

package com.guavapay.paymentsdk.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.WINDOW_ANIMATION_DURATION
import com.guavapay.paymentsdk.presentation.navigation.Navigation
import com.guavapay.paymentsdk.presentation.navigation.Route
import com.guavapay.paymentsdk.presentation.navigation.rememberNavBackStack
import com.guavapay.paymentsdk.presentation.platform.LocalParentScrollState
import com.guavapay.paymentsdk.presentation.platform.LocalTokensProvider
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme

@Composable internal fun PaymentGatewayContent(isOverlayLayoutVisible: Boolean, dismiss: (PaymentResult) -> Unit) {
  val nav = rememberNavBackStack<Route>(Route.HomeRoute)
  val dialogs = rememberNavBackStack<Route>()
  val showDialog = dialogs.isNotEmpty()

  fun cancel() {
    if (dialogs.lastOrNull() is Route.AbortRoute) {
      dialogs.removeLastOrNull()
      dismiss(PaymentResult.Cancel)
    } else if (dialogs.isNotEmpty()) {
      dialogs.removeLastOrNull()
    } else if (dialogs.isEmpty()) {
      dialogs.add(Route.CancelRoute)
    } else {
      dialogs.removeLastOrNull()
      dismiss(PaymentResult.Cancel)
    }
  }

  BackHandler(onBack = ::cancel)

  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
    AnimatedVisibility(
      visible = isOverlayLayoutVisible,
      enter = fadeIn(animationSpec = tween(WINDOW_ANIMATION_DURATION)),
      exit = fadeOut(animationSpec = tween(WINDOW_ANIMATION_DURATION))
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.scrim)
          .pointerInput(Unit) {
            detectTapGestures { cancel() }
          }
      )
    }

    AnimatedVisibility(
      visible = isOverlayLayoutVisible,
      enter = slideInVertically(
        animationSpec = tween(WINDOW_ANIMATION_DURATION),
        initialOffsetY = { it }
      ),
      exit = slideOutVertically(
        animationSpec = tween(WINDOW_ANIMATION_DURATION),
        targetOffsetY = { it }
      )
    ) { BottomSheetCard(nav = nav, dialogs = dialogs, dismiss = dismiss) }

    // Dialog overlay
    AnimatedVisibility(
      visible = showDialog,
      enter = scaleIn(
        animationSpec = tween(WINDOW_ANIMATION_DURATION),
        initialScale = 0.9f
      ) + fadeIn(animationSpec = tween(WINDOW_ANIMATION_DURATION)),
      exit = scaleOut(
        animationSpec = tween(WINDOW_ANIMATION_DURATION),
        targetScale = 0.9f
      ) + fadeOut(animationSpec = tween(WINDOW_ANIMATION_DURATION))
    ) {
      val route = remember { dialogs.lastOrNull() }
      if (route != null) {
        DialogOverlay(route = route, nav = nav, dialogRoutes = dialogs, dismiss = dismiss)
      }
    }
  }
}

@Composable private fun BottomSheetCard(nav: SnapshotStateList<Route>, dialogs: SnapshotStateList<Route>, dismiss: (PaymentResult) -> Unit) {
  val hasDialog = dialogs.isNotEmpty()

  var cardSize by remember { mutableStateOf(IntSize.Zero) }
  val cardHeightDp = with(LocalDensity.current) { cardSize.height.toDp() }
  val maxCardHeight = with(LocalDensity.current) {
    (LocalWindowInfo.current.containerSize.height * 0.9f).toDp()
  }

  val maxCardWidth = 500.dp

  val targetOffsetPx = if (hasDialog && cardSize.height > 0) cardSize.height.toFloat() else 0f
  val offsetY by animateFloatAsState(
    targetValue = targetOffsetPx,
    animationSpec = tween(WINDOW_ANIMATION_DURATION),
    label = "sheet-offset"
  )

  val cardColors = LocalTokensProvider.current.card()
  val kb = LocalSoftwareKeyboardController.current

  LaunchedEffect(hasDialog) {
    if (hasDialog && kb != null) kb.hide()
  }

  Box(Modifier.widthIn(max = maxCardWidth)) {
    Box(Modifier.graphicsLayer { translationY = offsetY }) {
      // Дикий костыль, зато красиво! Подложка под анимированную карточку, повторяющая размер карты.
      Box(
        Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .height(cardHeightDp)
          .background(
            cardColors.containerColor, shape = MaterialTheme.shapes.extraLarge.copy(
              bottomStart = CornerSize(0),
              bottomEnd = CornerSize(0)
            )
          )
      )

      // Основная карточка
      Column(
        modifier = Modifier
        .align(Alignment.BottomCenter)
        .onSizeChanged { cardSize = it }
        .fillMaxWidth()
        .heightIn(max = maxCardHeight)
      ) {
        CompositionLocalProvider(LocalParentScrollState provides rememberScrollState()) {
          Card(
            modifier = Modifier
              .shadow(0.dp)
              .clip(
                MaterialTheme.shapes.extraLarge.copy(
                  bottomStart = CornerSize(0),
                  bottomEnd = CornerSize(0)
                )
              )
              .animateContentSize(
                animationSpec = tween(WINDOW_ANIMATION_DURATION)
              )
              .verticalScroll(LocalParentScrollState.current),
            colors = cardColors,
            elevation = CardDefaults.cardElevation(0.dp),
            shape = MaterialTheme.shapes.extraLarge.copy(
              bottomStart = CornerSize(0),
              bottomEnd = CornerSize(0)
            )
          ) {
            Navigation(
              nav = nav,
              dialogs = dialogs,
              actions = Navigation.Actions(finish = dismiss)
            )

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime))
          }
        }
      }
    }
  }
}

@Composable private fun DialogOverlay(route: Route, nav: SnapshotStateList<Route>, dialogRoutes: SnapshotStateList<Route>, dismiss: (PaymentResult) -> Unit) {
  val maxCardWidth = 500.dp

  CompositionLocalProvider(LocalParentScrollState provides rememberScrollState()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(LocalParentScrollState.current)
        .pointerInput(Unit) {
          detectTapGestures {
            dialogRoutes.removeLastOrNull()
          }
        },
      contentAlignment = Alignment.Center
    ) {
      Column(modifier = Modifier.align(Alignment.Center).widthIn(max = maxCardWidth)) {
        Card(
          modifier = Modifier
            .padding(24.dp)
            .pointerInput(Unit) {
              detectTapGestures { /* Prevent click through */ }
            },
          colors = LocalTokensProvider.current.card(),
          elevation = CardDefaults.cardElevation(8.dp),
          shape = MaterialTheme.shapes.extraLarge,
        ) {
          Navigation.DialogContent(
            route = route,
            nav = nav,
            dialogRoutes = dialogRoutes,
            actions = Navigation.Actions(finish = dismiss)
          )
        }

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime))
      }
    }
  }
}

@PreviewLightDark @Composable private fun PaymentGatewayContentPreview() {
  PreviewTheme {
    PaymentGatewayContent(isOverlayLayoutVisible = true, dismiss = {})
  }
}
