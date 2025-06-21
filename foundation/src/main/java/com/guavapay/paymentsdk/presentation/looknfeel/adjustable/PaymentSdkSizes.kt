package com.guavapay.paymentsdk.presentation.looknfeel.adjustable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.presentation.platform.PaymentGatewaySizesProvider
import com.guavapay.paymentsdk.presentation.platform.PaymentGatewaySizesProvider.ButtonSizes
import com.guavapay.paymentsdk.presentation.platform.PaymentGatewaySizesProvider.CheckboxSizes
import com.guavapay.paymentsdk.presentation.platform.PaymentGatewaySizesProvider.TextFieldSizes

open class PaymentSdkSizes : PaymentGatewaySizesProvider {
  @ReadOnlyComposable @Composable override fun textfield() = TextFieldSizes(48.dp)
  @ReadOnlyComposable @Composable override fun button() = ButtonSizes(56.dp)
  @ReadOnlyComposable @Composable override fun checkbox() = CheckboxSizes(24.dp)
}