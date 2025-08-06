package com.guavapay.paymentsdk.gateway.banking

import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.presentation.platform.ComposeString
import java.io.Serializable
import androidx.compose.ui.res.stringResource as string

/**
 * Represents the purpose of a payment, often used for labeling UI elements like buttons.
 *
 * @property text The display text for the payment kind, as a composable string.
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
sealed class PaymentKind(open val text: ComposeString) : Serializable {
  /** For booking a service or item. */
  data object Book : PaymentKind({ string(R.string.payment_kind_book) }) { private fun readResolve(): Any = Book }
  /** For buying an item. */
  data object Buy : PaymentKind({ string(R.string.payment_kind_buy) }) { private fun readResolve(): Any = Buy }
  /** For a checkout process. */
  data object Checkout : PaymentKind({ string(R.string.payment_kind_checkout) }) { private fun readResolve(): Any = Checkout }
  /** For making a donation. */
  data object Donate : PaymentKind({ string(R.string.payment_kind_donate) }) { private fun readResolve(): Any = Donate }
  /** For placing an order. */
  data object Order : PaymentKind({ string(R.string.payment_kind_order) }) { private fun readResolve(): Any = Order }
  /** For a general payment. */
  data object Pay : PaymentKind({ string(R.string.payment_kind_pay) }) { private fun readResolve(): Any = Pay }
  /** A plain or un-categorized payment. */
  data object Plain : PaymentKind({ string(R.string.payment_kind_plain) }) { private fun readResolve(): Any = Plain }
  /** For starting a subscription. */
  data object Subscribe : PaymentKind({ string(R.string.payment_kind_subscribe) }) { private fun readResolve(): Any = Subscribe }

  /**
   * A custom payment kind with user-defined text.
   *
   * @property text The custom display text for the payment kind.
   * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
   */
  data class Custom(override val text: ComposeString) : PaymentKind(text)
}
