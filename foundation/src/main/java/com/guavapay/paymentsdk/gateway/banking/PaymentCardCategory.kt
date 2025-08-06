package com.guavapay.paymentsdk.gateway.banking

/**
 * Represents the category of a payment card.
 *
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
enum class PaymentCardCategory {
  /** A debit card, where funds are drawn directly from a bank account. */
  DEBIT,

  /** A credit card, which allows borrowing funds to be paid back later. */
  CREDIT,

  /** A prepaid card, which is loaded with a specific amount of money. */
  PREPAID
}
