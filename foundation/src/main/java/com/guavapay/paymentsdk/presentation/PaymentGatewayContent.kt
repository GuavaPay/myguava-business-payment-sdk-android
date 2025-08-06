package com.guavapay.paymentsdk.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.WINDOW_ANIMATION_DURATION
import com.guavapay.paymentsdk.presentation.navigation.Navigation
import com.guavapay.paymentsdk.presentation.navigation.Route
import com.guavapay.paymentsdk.presentation.navigation.rememberNavBackStack
import com.guavapay.paymentsdk.presentation.platform.LocalTokensProvider
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme

@Composable internal fun PaymentGatewayContent(isOverlayLayoutVisible: Boolean, dismiss: (PaymentResult) -> Unit) {
  val nav = rememberNavBackStack<Route>(Route.HomeRoute)

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
            detectTapGestures {
              if (nav.lastOrNull() != Route.CancelRoute) {
                nav.add(Route.CancelRoute)
              } else {
                dismiss(PaymentResult.Cancel)
              }
            }
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
    ) { BottomSheetCard(nav = nav, dismiss = dismiss) }
  }
}

@Composable
private fun BottomSheetCard(nav: SnapshotStateList<Route>, dismiss: (PaymentResult) -> Unit) {
  var cardSize by remember { mutableStateOf(IntSize.Zero) }
  val cardHeightDp = with(LocalDensity.current) { cardSize.height.toDp() }
  val maxCardHeight = with(LocalDensity.current) {
    (LocalWindowInfo.current.containerSize.height * 0.9f).toDp()
  }

  val cardColors = LocalTokensProvider.current.card()

  Box(Modifier.wrapContentSize(Alignment.BottomStart)) {
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

    Card(
      modifier = Modifier
        .onSizeChanged { cardSize = it }
        .fillMaxWidth()
        .heightIn(
          max = maxCardHeight
        )
        .windowInsetsPadding(WindowInsets.ime)
        .shadow(0.dp)
        .clip(
          MaterialTheme.shapes.extraLarge.copy(
            bottomStart = CornerSize(0),
            bottomEnd = CornerSize(0)
          )
        )
        .animateContentSize(
          animationSpec = tween(WINDOW_ANIMATION_DURATION)
        ),
      colors = cardColors,
      elevation = CardDefaults.cardElevation(0.dp),
      shape = MaterialTheme.shapes.extraLarge.copy(
        bottomStart = CornerSize(0),
        bottomEnd = CornerSize(0)
      )
    ) {
      Navigation(
        nav = nav,
        actions = Navigation.Actions(finish = dismiss)
      )
    }
  }
}

@PreviewLightDark @Composable private fun PaymentGatewayContentPreview() {
  PreviewTheme {
    PaymentGatewayContent(isOverlayLayoutVisible = true, dismiss = {})
  }
}
