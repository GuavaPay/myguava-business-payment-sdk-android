package com.guavapay.paymentsdk.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.presentation.PaymentGatewayActivity.Companion.WINDOW_ANIMATION_DURATION
import com.guavapay.paymentsdk.presentation.platform.getDialogWindow
import com.guavapay.paymentsdk.rememberLibraryUnit
import kotlinx.coroutines.delay

@Composable internal fun PaymentGatewayBottomSheet(result: (PaymentResult) -> Unit) {
  val lib = rememberLibraryUnit()

  lib.state.payload().looknfeel.Decorate {
    var isWindowVisible by remember { mutableStateOf(true) }
    var isOverlayLayoutVisible by remember { mutableStateOf(false) }
    var pendingResult by remember { mutableStateOf<PaymentResult?>(null) }

    LaunchedEffect(Unit) {
      delay(50) // Slide-up animation start delay
      isOverlayLayoutVisible = true
    }

    LaunchedEffect(isOverlayLayoutVisible, pendingResult) {
      if (!isOverlayLayoutVisible && pendingResult != null) {
        delay(WINDOW_ANIMATION_DURATION.toLong()) // Slide-down animation delay
        isWindowVisible = false
        result(pendingResult!!)
      }
    }

    fun close(result: PaymentResult) {
      pendingResult = result
      isOverlayLayoutVisible = false
    }

    BackHandler(enabled = isWindowVisible) { close(PaymentResult.Canceled) }

    if (isWindowVisible) {
      Dialog(
        onDismissRequest = { close(PaymentResult.Canceled) },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
      ) {
        val dialogWindow = getDialogWindow()
        SideEffect {
          dialogWindow?.setDimAmount(0f)
          dialogWindow?.setWindowAnimations(-1)
        }

        PaymentGatewayContent(isOverlayLayoutVisible, ::close)
      }
    }
  }
}