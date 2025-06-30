package com.guavapay.paymentsdk.gateway.vendors.googlepay

import com.google.android.gms.wallet.WalletConstants

internal enum class GPayEnvironment(internal val qualifier: Int) {
  TEST(WalletConstants.ENVIRONMENT_TEST),
  PRODUCTION(WalletConstants.ENVIRONMENT_PRODUCTION)
}