package com.guavapay.paymentsdk.gateway.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.guavapay.paymentsdk.gateway.banking.PaymentAmount
import com.guavapay.paymentsdk.gateway.banking.PaymentInstruments
import com.guavapay.paymentsdk.gateway.banking.PaymentKind
import java.io.Serializable

data class PaymentGatewayState(
  val merchant: String,
  val instruments: PaymentInstruments,
  val amount: PaymentAmount,
  val kind: PaymentKind = PaymentKind.Pay,
  val decorator: PaymentGatewayComposableDecorator = PaymentGatewayComposableDecorator { it() }
) : Serializable {
  @Stable fun interface PaymentGatewayComposableDecorator : Serializable {
    @Composable fun Decorate(content: @Composable () -> Unit)
  }

  companion object
}