package com.guavapay.paymentsdk.presentation.components.atomic

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayState
import com.guavapay.paymentsdk.gateway.vendors.googlepay.rememberGPayOrchestrator
import com.guavapay.paymentsdk.gateway.vendors.googlepay.toGoogleAllowedMethodsJson
import com.guavapay.paymentsdk.presentation.platform.LocalSizesProvider
import kotlinx.coroutines.launch

@Composable internal fun GooglePayButton(state: PaymentGatewayState, result: (PaymentResult) -> Unit) {
  val sizes = LocalSizesProvider.current
  val scope = rememberCoroutineScope()
  val orchestrator = rememberGPayOrchestrator(state)
  val isProcessing by orchestrator.isProcessing.collectAsStateWithLifecycle()
  val isReady by orchestrator.isReady.collectAsStateWithLifecycle()

  val gpay = remember(state.instruments) { state.instruments.instrument<PaymentMethod.GooglePay>() } ?: return
  val json = remember(state.instruments, state::toGoogleAllowedMethodsJson)
  val type = remember(gpay.ordertype) { ButtonType.entries.find { it.value == gpay.ordertype.qualifier } ?: ButtonType.Pay }

  com.google.pay.button.PayButton(
    onClick = { scope.launch { result(orchestrator.start()) } },
    allowedPaymentMethods = json,
    modifier = Modifier.fillMaxWidth().height(sizes.button().height),
    theme = if (isSystemInDarkTheme()) ButtonTheme.Light else ButtonTheme.Dark,
    type = type,
    radius = 8.dp,
    enabled = isReady && !isProcessing
  )
}