package com.guavapay.paymentsdk.gateway.vendors.googlepay

import com.google.android.gms.wallet.button.ButtonConstants.ButtonType.BOOK
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType.BUY
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType.CHECKOUT
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType.DONATE
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType.ORDER
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType.PAY
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType.PLAIN
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType.SUBSCRIBE

enum class GPayOrderType(internal val qualifier: Int) {
  Book(BOOK),
  Buy(BUY),
  Checkout(CHECKOUT),
  Donate(DONATE),
  Order(ORDER),
  Pay(PAY),
  Plain(PLAIN),
  Subscribe(SUBSCRIBE),
}