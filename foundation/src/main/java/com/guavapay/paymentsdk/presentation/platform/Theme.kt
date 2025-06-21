package com.guavapay.paymentsdk.presentation.platform

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CardColors
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import com.guavapay.paymentsdk.presentation.looknfeel.adjustable.PaymentSdkSizes
import com.guavapay.paymentsdk.presentation.looknfeel.adjustable.PaymentSdkTokens

val LocalTokensProvider = staticCompositionLocalOf<PaymentGatewayTokensProvider>(::PaymentSdkTokens)
val LocalSizesProvider = staticCompositionLocalOf<PaymentGatewaySizesProvider>(::PaymentSdkSizes)

@Immutable interface PaymentGatewayTokensProvider {
  @Composable fun textfield(): TextFieldColors
  @Composable fun button(): ButtonColors
  @Composable fun card(): CardColors
  @Composable fun checkbox(): CheckboxColors
  @Composable fun radio(): RadioButtonColors
  @Composable fun switch(): SwitchColors
}

@Immutable interface PaymentGatewaySizesProvider {
  @ReadOnlyComposable @Composable fun textfield(): TextFieldSizes
  @ReadOnlyComposable @Composable fun button(): ButtonSizes
  @ReadOnlyComposable @Composable fun checkbox(): CheckboxSizes

  data class TextFieldSizes(val height: Dp)
  data class ButtonSizes(val height: Dp)
  data class CheckboxSizes(val height: Dp)
}
