package com.guavapay.paymentsdk.gateway.vendors.googlepay

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.intl.Locale
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayState

@Composable internal fun rememberGPayOrchestrator(state: PaymentGatewayState): GPayOrchestrator {
  val context = LocalContext.current
  val orchestrator = remember(context, state) {
    val googlePay = state.instruments.instrument<PaymentMethod.GooglePay>()
    val locale = googlePay?.locale ?: Locale.current.platformLocale
    GPayOrchestrator(context, state, locale)
  }

  val launcher = rememberLauncherForActivityResult(StartIntentSenderForResult(), orchestrator::onActivityResult)

  LaunchedEffect(orchestrator, launcher) {
    orchestrator.setLauncher(launcher)
    orchestrator.rediness()
  }

  return orchestrator
}
