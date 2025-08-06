package com.guavapay.paymentsdk.gateway.banking

/**
 * Defines the operational environment for payment processing.
 *
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
enum class PaymentCircuit {
  /** For development and testing purposes, typically with mock data. */
  Development,

  /** A testing environment that mirrors the production environment but uses test credentials. */
  Sandbox,

  /** The live environment for processing real transactions. */
  Production
}
