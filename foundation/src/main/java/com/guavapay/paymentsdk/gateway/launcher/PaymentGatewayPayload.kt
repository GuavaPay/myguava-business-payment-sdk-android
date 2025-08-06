package com.guavapay.paymentsdk.gateway.launcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.guavapay.paymentsdk.gateway.banking.PaymentCardCategory
import com.guavapay.paymentsdk.gateway.banking.PaymentCardScheme
import com.guavapay.paymentsdk.gateway.banking.PaymentCircuit
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
 * @property circuit The payment environment to use. If null, the default circuit will be used.
 * @property kind The kind of payment, which affects the text on the pay button.
 * @property methods The set of allowed payment methods.
 * @property schemes The set of allowed card schemes.
 * @property categories The set of allowed card categories.
 * @property looknfeel A decorator for the main payment UI.
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
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
