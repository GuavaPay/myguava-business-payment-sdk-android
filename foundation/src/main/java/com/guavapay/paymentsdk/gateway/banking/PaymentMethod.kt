package com.guavapay.paymentsdk.gateway.banking

import java.io.Serializable

/**
 * Defines the available payment methods within the SDK.
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
sealed interface PaymentMethod : Serializable {
  /**
   * Provides a set of all standard payment method entries.
   */
  companion object {
    val Entries get() = setOf(PayPal, GooglePay, Card(), SavedCard())
  }

  /** Represents payment via PayPal. */
  data object PayPal : PaymentMethod { private fun readResolve(): Any = PayPal }

  /** Represents payment via Google Pay. */
  data object GooglePay : PaymentMethod { private fun readResolve(): Any = GooglePay }

  /**
   * Represents payment using a new card.
   *
   * @property flags Configuration flags for the new card input form.
   */
  data class Card(val flags: Flags = Flags()) : PaymentMethod {
    /**
     * Configuration flags for the new card payment method.
     *
     * @property allowScan Determines whether card scanning is enabled.
     * @property allowCardHolderName Determines whether the cardholder name field is displayed.
     */
    data class Flags(val allowScan: Boolean = true, val allowCardHolderName: Boolean = true) : Serializable
  }

  /**
   * Represents payment using a previously saved card.
   *
   * @property flags Configuration flags for the saved card selection.
   */
  data class SavedCard(val flags: Flags = Flags()) : PaymentMethod {
    /**
     * Configuration flags for the saved card payment method.
     *
     * @property allowEdit Determines whether editing saved cards is permitted.
     * @property allowRemove Determines whether removing saved cards is permitted.
     * @property showUnavailable Determines whether to show cards that are not currently available for payment.
     */
    data class Flags(val allowEdit: Boolean = true, val allowRemove: Boolean = true, val showUnavailable: Boolean = false) : Serializable
  }
}
