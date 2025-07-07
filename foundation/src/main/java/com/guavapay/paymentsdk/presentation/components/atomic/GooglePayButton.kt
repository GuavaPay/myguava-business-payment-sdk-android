package com.guavapay.paymentsdk.presentation.components.atomic

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import com.guavapay.paymentsdk.gateway.banking.PaymentKind
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayCoroutineScope
import com.guavapay.paymentsdk.gateway.vendors.googlepay.GPayOrchestrator
import com.guavapay.paymentsdk.gateway.vendors.googlepay.GPayResult
import com.guavapay.paymentsdk.presentation.platform.LocalSizesProvider
import com.guavapay.paymentsdk.presentation.platform.remember
import com.guavapay.paymentsdk.rememberLibraryUnit
import kotlinx.coroutines.launch

@Composable internal fun GooglePayButton(orchestrator: GPayOrchestrator, result: (GPayResult) -> Unit) {
  val isReady by orchestrator.isReady.collectAsState()
  val isProcessing by orchestrator.isProcessing.collectAsState()
  val sizes = LocalSizesProvider.current

  val type = remember(rememberLibraryUnit().state.payload?.kind) {
    when (it) {
      is PaymentKind.Book -> ButtonType.Book
      is PaymentKind.Buy -> ButtonType.Buy
      is PaymentKind.Checkout -> ButtonType.Checkout
      is PaymentKind.Custom -> ButtonType.Plain
      is PaymentKind.Donate -> ButtonType.Donate
      is PaymentKind.Order -> ButtonType.Order
      is PaymentKind.Pay -> ButtonType.Pay
      is PaymentKind.Plain -> ButtonType.Plain
      is PaymentKind.Subscribe -> ButtonType.Subscribe
      null -> ButtonType.Pay
    }
  }

  com.google.pay.button.PayButton(
    onClick = { PaymentGatewayCoroutineScope().launch { result(orchestrator.start()) } },
    allowedPaymentMethods = orchestrator.buttonPayload,
    modifier = Modifier.fillMaxWidth().height(sizes.button().height),
    theme = if (isSystemInDarkTheme()) ButtonTheme.Light else ButtonTheme.Dark,
    type = type,
    radius = 8.dp,
    enabled = isReady && !isProcessing
  )
}