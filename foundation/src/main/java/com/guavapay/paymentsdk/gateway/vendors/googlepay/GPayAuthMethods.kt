package com.guavapay.paymentsdk.gateway.vendors.googlepay

/**
 * Defines the authentication methods for Google Pay.
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
enum class GPayAuthMethods {
  /**
   * Card authentication will be done using a PAN (Primary Account Number) only.
   * This is a less secure method.
   */
  PAN_ONLY,

  /**
   * Card authentication will be done using a cryptogram and 3D-Secure.
   * This is a more secure method.
   */
  CRYPTOGRAM_3DS
}
