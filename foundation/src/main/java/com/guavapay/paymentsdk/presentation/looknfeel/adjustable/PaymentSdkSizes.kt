package com.guavapay.paymentsdk.presentation.looknfeel.adjustable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.presentation.platform.PaymentGatewaySizesProvider
import com.guavapay.paymentsdk.presentation.platform.PaymentGatewaySizesProvider.ButtonSizes
import com.guavapay.paymentsdk.presentation.platform.PaymentGatewaySizesProvider.CheckboxSizes
import com.guavapay.paymentsdk.presentation.platform.PaymentGatewaySizesProvider.TextFieldSizes

/**
 * Default implementation of [PaymentGatewaySizesProvider] for the Payment SDK.
 *
 * This class provides default dimensions for various UI components.
 * It can be extended to customize the sizes.
 *
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
open class PaymentSdkSizes : PaymentGatewaySizesProvider {
  /**
   * Provides the default sizes for a text field.
   * @return [TextFieldSizes] with a height of 48dp.
   */
  @ReadOnlyComposable @Composable override fun textfield() = TextFieldSizes(48.dp)

  /**
   * Provides the default sizes for a button.
   * @return [ButtonSizes] with a height of 48dp.
   */
  @ReadOnlyComposable @Composable override fun button() = ButtonSizes(48.dp)

  /**
   * Provides the default sizes for a checkbox.
   * @return [CheckboxSizes] with a height of 24dp.
   */
  @ReadOnlyComposable @Composable override fun checkbox() = CheckboxSizes(24.dp)
}
