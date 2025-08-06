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

/**
 * A `CompositionLocal` that provides token values for theming payment gateway components.
 * @see PaymentGatewayTokensProvider
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
val LocalTokensProvider = staticCompositionLocalOf<PaymentGatewayTokensProvider>(::PaymentSdkTokens)

/**
 * A `CompositionLocal` that provides size values for theming payment gateway components.
 * @see PaymentGatewaySizesProvider
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
val LocalSizesProvider = staticCompositionLocalOf<PaymentGatewaySizesProvider>(::PaymentSdkSizes)

/**
 * An interface for providing theme tokens (colors) for various UI components within the payment gateway.
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
@Immutable interface PaymentGatewayTokensProvider {
  /** Returns the colors for a text field. */
  @Composable fun textfield(): TextFieldColors
  /** Returns the colors for a button. */
  @Composable fun button(): ButtonColors
  /** Returns the colors for a card. */
  @Composable fun card(): CardColors
  /** Returns the colors for a checkbox. */
  @Composable fun checkbox(): CheckboxColors
  /** Returns the colors for a radio button. */
  @Composable fun radio(): RadioButtonColors
  /** Returns the colors for a switch. */
  @Composable fun switch(): SwitchColors
}

/**
 * An interface for providing theme sizes for various UI components within the payment gateway.
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
@Immutable interface PaymentGatewaySizesProvider {
  /** Returns the sizes for a text field. */
  @ReadOnlyComposable @Composable fun textfield(): TextFieldSizes
  /** Returns the sizes for a button. */
  @ReadOnlyComposable @Composable fun button(): ButtonSizes
  /** Returns the sizes for a checkbox. */
  @ReadOnlyComposable @Composable fun checkbox(): CheckboxSizes

  /**
   * Defines the sizes for a text field.
   * @property height The height of the text field.
   */
  data class TextFieldSizes(val height: Dp)

  /**
   * Defines the sizes for a button.
   * @property height The height of the button.
   */
  data class ButtonSizes(val height: Dp)

  /**
   * Defines the sizes for a checkbox.
   * @property height The height of the checkbox.
   */
  data class CheckboxSizes(val height: Dp)
}
