package com.guavapay.paymentsdk.gateway.vendors.googlepay

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.intl.Locale
import com.guavapay.paymentsdk.network.services.OrderApi.Models.GooglePayContext
import com.guavapay.paymentsdk.network.services.OrderApi.Models.Order

@Composable internal fun rememberGPayOrchestrator(order: Order, gpayctx: GooglePayContext): GPayOrchestrator {
  val context = LocalContext.current
  val orchestrator = remember(context, order, gpayctx) {
    val locale = Locale.current.platformLocale
    GPayOrchestrator(context, order, gpayctx, locale)
  }

  val launcher = rememberLauncherForActivityResult(StartIntentSenderForResult(), orchestrator::onActivityResult)

  LaunchedEffect(orchestrator, launcher) {
    orchestrator.setLauncher(launcher)
    orchestrator.initialize()
  }

  return orchestrator
}
