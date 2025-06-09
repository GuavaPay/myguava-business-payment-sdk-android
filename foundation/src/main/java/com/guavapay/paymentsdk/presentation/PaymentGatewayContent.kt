package com.guavapay.paymentsdk.presentation

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.gateway.banking.PaymentAmount
import com.guavapay.paymentsdk.gateway.banking.PaymentInstruments
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayState
import com.guavapay.paymentsdk.platform.compose.PreviewTheme
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.WINDOW_ANIMATION_DURATION

@Composable internal fun PaymentGatewayContent(state: PaymentGatewayState, isOverlayLayoutVisible: Boolean, dismiss: (PaymentResult) -> Unit) {
  val configuration = LocalConfiguration.current
  val maxHeight = configuration.screenHeightDp.dp * 0.9f

  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
    AnimatedVisibility(
      visible = isOverlayLayoutVisible,
      enter = fadeIn(animationSpec = tween(WINDOW_ANIMATION_DURATION)),
      exit = fadeOut(animationSpec = tween(WINDOW_ANIMATION_DURATION))
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.5f))
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
          .heightIn(max = maxHeight)
          .windowInsetsPadding(WindowInsets.ime)
          .shadow(8.dp, shape = MaterialTheme.shapes.extraLarge)
          .clip(MaterialTheme.shapes.extraLarge),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.extraLarge
      ) {
        PaymentGatewayMainPage(state = state, dismiss = dismiss)
      }
    }
  }
}

@PreviewLightDark @Composable private fun PaymentGatewayContentPreview() {
  PreviewTheme {
    PaymentGatewayContent(
      state = PaymentGatewayState(
        merchant = "Demo Store",
        instruments = PaymentInstruments(),
        amount = PaymentAmount(
          java.math.BigDecimal("20.00"),
          java.util.Currency.getInstance(java.util.Locale.US)
        )
      ),
      isOverlayLayoutVisible = true,
      dismiss = {}
    )
  }
}
