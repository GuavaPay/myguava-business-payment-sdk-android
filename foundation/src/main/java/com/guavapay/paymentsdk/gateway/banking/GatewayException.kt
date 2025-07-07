package com.guavapay.paymentsdk.gateway.banking

sealed class GatewayException(open val throwable: Throwable? = null, override val message: String = throwable?.message.toString()) : Exception(message, throwable) {
  class UnknownException(throwable: Throwable) : GatewayException(throwable)

  sealed class ClientException(override val throwable: Throwable? = null, override val message: String = throwable?.message.toString()) : GatewayException(throwable, message) {
    class NoAvailableCardSchemesException(message: String) : ClientException(message = message)
    class NoAvailableCardProductCategoriesException(message: String) : ClientException(message = message)
  }
}
