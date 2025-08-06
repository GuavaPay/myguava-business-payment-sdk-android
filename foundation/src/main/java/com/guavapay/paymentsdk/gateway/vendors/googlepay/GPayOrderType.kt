package com.guavapay.paymentsdk.gateway.vendors.googlepay

import com.google.android.gms.wallet.button.ButtonConstants.ButtonType.BOOK
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType.BUY
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType.CHECKOUT
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType.DONATE
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType.ORDER
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType.PAY
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType.PLAIN
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType.SUBSCRIBE

/**
 * Represents the type of Google Pay button to be displayed.
 *
 * This enum maps to the `ButtonType` constants from the Google Pay API.
 *
 * @property qualifier The integer qualifier for the Google Pay button type.
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
enum class GPayOrderType(internal val qualifier: Int) {
  /** Corresponds to `ButtonType.BOOK`. */
  Book(BOOK),

  /** Corresponds to `ButtonType.BUY`. */
  Buy(BUY),

  /** Corresponds to `ButtonType.CHECKOUT`. */
  Checkout(CHECKOUT),

  /** Corresponds to `ButtonType.DONATE`. */
  Donate(DONATE),

  /** Corresponds to `ButtonType.ORDER`. */
  Order(ORDER),

  /** Corresponds to `ButtonType.PAY`. */
  Pay(PAY),

  /** Corresponds to `ButtonType.PLAIN`. */
  Plain(PLAIN),

  /** Corresponds to `ButtonType.SUBSCRIBE`. */
  Subscribe(SUBSCRIBE),
}
