package com.guavapay.paymentsdk.gateway.banking

import com.guavapay.paymentsdk.R

/**
 * Represents a payment card scheme (e.g., Visa, Mastercard).
 *
 * @property image The drawable resource ID for the card scheme's logo.
 * @property pan The expected length of the Primary Account Number (PAN).
 * @property cvc The expected length of the Card Verification Code (CVC).
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
enum class PaymentCardScheme(val image: Int, val pan: Int, val cvc: Int) {
  /** Visa card scheme. */
  VISA(R.drawable.ic_logo_cn_visa, 16, 3),

  /** Mastercard card scheme. */
  MASTERCARD(R.drawable.ic_logo_cn_mastercard, 16, 3),

  /** UnionPay card scheme. */
  UNIONPAY(R.drawable.ic_logo_cn_unionpay, 16, 3),

  /** American Express card scheme. */
  AMERICAN_EXPRESS(R.drawable.ic_logo_cn_amex, 15, 4),

  /** Diners Club card scheme. */
  DINERS_CLUB(R.drawable.ic_logo_cn_dinersclub, 19, 3)
}
