package com.guavapay.paymentsdk.gateway.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.guavapay.paymentsdk.gateway.banking.PaymentCardCategory
import com.guavapay.paymentsdk.gateway.banking.PaymentCardScheme
import com.guavapay.paymentsdk.gateway.banking.PaymentCircuit
import com.guavapay.paymentsdk.gateway.banking.PaymentKind
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod
import com.guavapay.paymentsdk.presentation.looknfeel.PrebuiltSdkTheme
import com.myguava.android.myguava3ds2.init.ui.GUiCustomization
import java.io.Serializable
import java.util.Locale

data class PaymentGatewayPayload(
  val orderId: String,
  val sessionToken: String,
  val locale: Locale? = null,
  val circuit: PaymentCircuit? = null,
  val kind: PaymentKind = PaymentKind.Pay,
  val methods: Set<PaymentMethod> = PaymentMethod.Entries,
  val schemes: Set<PaymentCardScheme> = PaymentCardScheme.entries.toSet(),
  val categories: Set<PaymentCardCategory> = PaymentCardCategory.entries.toSet(),
  val threedsLooknfeel: GUiCustomization? = null,
  val looknfeel: PaymentGatewayLooknfeel = PaymentGatewayLooknfeel { PrebuiltSdkTheme { it() } },
) : Serializable {
  @Stable fun interface PaymentGatewayLooknfeel : Serializable {
    @Composable fun Decorate(content: @Composable () -> Unit)
  }

  inline fun locale() = locale!! // Never will happen. Initialized by PaymentGateway

  companion object
}