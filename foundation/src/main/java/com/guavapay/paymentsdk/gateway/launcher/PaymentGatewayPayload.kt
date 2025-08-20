package com.guavapay.paymentsdk.gateway.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.annotation.RememberInComposition
import com.guavapay.paymentsdk.gateway.banking.PaymentCardCategory
import com.guavapay.paymentsdk.gateway.banking.PaymentCardScheme
import com.guavapay.paymentsdk.gateway.banking.PaymentEnvironment
import com.guavapay.paymentsdk.gateway.banking.PaymentKind
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod
import com.guavapay.paymentsdk.presentation.looknfeel.PrebuiltSdkTheme
import com.guavapay.myguava.business.myguava3ds2.init.ui.GUiCustomization
import java.io.Serializable
import java.util.Locale

/**
 * Data class that holds all the necessary configuration and data for a payment session.
 *
 * @property orderId The unique identifier for the order.
 * @property sessionToken A token for the user's session.
 * @property locale The locale to be used for the UI. If null, the system locale will be used.
 * @property environment The payment environment to use. If null, the default circuit will be used.
 * @property kind The kind of payment, which affects the text on the pay button.
 * @property availablePaymentMethods The set of allowed payment methods.
 * @property availableCardSchemes The set of allowed card schemes.
 * @property availableCardProductCategories The set of allowed card categories.
 * @property looknfeel A decorator for the main payment UI.
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
data class PaymentGatewayPayload @RememberInComposition constructor(
  val orderId: String,
  val sessionToken: String,
  val locale: Locale? = null,
  val environment: PaymentEnvironment? = null,
  val availablePaymentMethods: Set<PaymentMethod> = PaymentMethod.Entries,
  val availableCardSchemes: Set<PaymentCardScheme> = PaymentCardScheme.entries.toSet(),
  val availableCardProductCategories: Set<PaymentCardCategory> = PaymentCardCategory.entries.toSet(),
  val threedsLooknfeel: GUiCustomization? = null,
  val looknfeel: PaymentGatewayLooknfeel = PaymentGatewayLooknfeel { PrebuiltSdkTheme { it() } },
) : Serializable {
  // todo: moved from ctor. (for backward internal compatibility).
  // todo: it need to remove after moment when kind will available as result from BE side.
  internal val kind: PaymentKind = PaymentKind.Pay

  /**
   * A functional interface to decorate the payment screen's UI.
   * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
   */
  @Stable fun interface PaymentGatewayLooknfeel : Serializable {
    /**
     * A composable function that wraps the content of the payment screen.
     *
     * @param content The content to be decorated.
     */
    @Composable fun Decorate(content: @Composable () -> Unit)
  }

  internal inline fun locale() = locale!! // Never will happen. Initialized by PaymentGateway
  internal inline fun threedsLooknfeel() = threedsLooknfeel!! // Never will happen. Initialized by PaymentGateway

  companion object
}
