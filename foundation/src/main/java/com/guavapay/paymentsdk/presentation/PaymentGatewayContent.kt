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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.WINDOW_ANIMATION_DURATION
import com.guavapay.paymentsdk.presentation.navigation.Navigation
import com.guavapay.paymentsdk.presentation.platform.LocalTokensProvider
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme

@Composable internal fun PaymentGatewayContent(isOverlayLayoutVisible: Boolean, dismiss: (PaymentResult) -> Unit) {
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
            detectTapGestures { dismiss(PaymentResult.Canceled) }
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
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = LocalWindowInfo.current.containerSize.height.dp * 0.9f)
          .windowInsetsPadding(WindowInsets.ime)
          .shadow(0.dp)
          .clip(MaterialTheme.shapes.extraLarge)
          .animateContentSize(animationSpec = tween(durationMillis = WINDOW_ANIMATION_DURATION)),
        colors = LocalTokensProvider.current.card(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.extraLarge.copy(bottomStart = CornerSize(0), bottomEnd = CornerSize(0))
      ) {
        Navigation(actions = Navigation.Actions(finish = dismiss))
      }
    }
  }
}

@PreviewLightDark @Composable private fun PaymentGatewayContentPreview() {
  PreviewTheme {
    PaymentGatewayContent(isOverlayLayoutVisible = true, dismiss = {})
  }
}
