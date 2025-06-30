package com.guavapay.paymentsdk.gateway.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.guavapay.paymentsdk.gateway.banking.PaymentCardCategory
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetwork
import com.guavapay.paymentsdk.gateway.banking.PaymentKind
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod
import com.guavapay.paymentsdk.presentation.looknfeel.PrebuiltSdkTheme
import java.util.Locale
import java.io.Serializable

data class PaymentGatewayPayload(
  val orderId: String,
  val sessionToken: String,
  val locale: Locale? = null,
  val kind: PaymentKind = PaymentKind.Pay,
  val methods: Set<PaymentMethod> = PaymentMethod.Entries,
  val networks: Set<PaymentCardNetwork> = PaymentCardNetwork.entries.toSet(),
  val categories: Set<PaymentCardCategory> = PaymentCardCategory.entries.toSet(),
  val looknfeel: PaymentGatewayLooknfeel = PaymentGatewayLooknfeel { PrebuiltSdkTheme { it() } },
) : Serializable {
  @Stable fun interface PaymentGatewayLooknfeel : Serializable {
    @Composable fun Decorate(content: @Composable () -> Unit)
  }

  inline fun locale() = locale!! // Never will happen. Initialized by PaymentGateway

  companion object
}