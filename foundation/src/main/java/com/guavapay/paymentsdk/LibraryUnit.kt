package com.guavapay.paymentsdk

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.startup.AppInitializer
import com.guavapay.paymentsdk.gateway.banking.PaymentCircuit
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayPayload
import com.guavapay.paymentsdk.logging.i
import com.guavapay.paymentsdk.metrica.MetricaUnit
import com.guavapay.paymentsdk.network.NetworkUnit
import com.guavapay.paymentsdk.platform.coroutines.CoroutineUnit
import com.guavapay.paymentsdk.platform.function.lazy
import com.guavapay.paymentsdk.presentation.navigation.NavigationEvents
import com.guavapay.paymentsdk.presentation.platform.remember
import kotlin.reflect.KProperty0

@Composable internal fun rememberLibraryUnit() = if (LocalInspectionMode.current) {
  LibraryUnit(LocalContext.current).apply {
    state.payload = PaymentGatewayPayload(
      orderId = "<null>",
      sessionToken = "<null>",
      locale = androidx.compose.ui.text.intl.Locale.current.platformLocale,
      circuit = PaymentCircuit.Development,
      threedsLooknfeel = null,
    )
  }
} else {
  remember(LocalContext.current, LibraryUnit::from)
}

internal class LibraryUnit(val context: Context) {
  val state by lazy(::LibraryState)
  val coroutine by lazy(::CoroutineUnit)
  val network by lazy(::NetworkUnit)
  val metrica by lazy(::MetricaUnit)
  val navigation by lazy(::NavigationEvents)

  private val initialize = listOf(::state, ::coroutine, ::network, ::metrica, ::navigation)
  fun initialize(): LibraryUnit {
    i("Starting initialization of library components")
    initialize.forEach(KProperty0<*>::invoke)
    metrica.breadcrumb("Pre-Initialized", category = "Sdk Lifecycle", type = "info")
    i("Finished initialization of library components")
    return this
  }

  companion object {
    fun from(context: Context) = AppInitializer
      .getInstance(context)
      .initializeComponent(LibraryZygote::class.java)
  }
}
