package com.guavapay.paymentsdk.gateway.banking

sealed class GatewayException(open val throwable: Throwable? = null, override val message: String = throwable?.message.toString()) : Exception(message, throwable) {
  class UnknownException(throwable: Throwable) : GatewayException(throwable)

  sealed class GooglePayException(override val throwable: Throwable? = null, override val message: String = throwable?.message.toString()) : GatewayException(throwable, message) {
    class GooglePayNoPaymentDataException() : GooglePayException()
    class GooglePayApiException(val code: Int?, message: String) : GooglePayException(message = message)
    class GooglePayUnknownException(message: String) : GooglePayException(message = message)
    class GooglePayNotReadyException(message: String) : GooglePayException(message = message)
    class GooglePayNotInitializedException(message: String) : GooglePayException(message = message)
  }

  sealed class ClientException(override val throwable: Throwable? = null, override val message: String = throwable?.message.toString()) : GatewayException(throwable, message) {
    class NoAvailableCardSchemesException(message: String) : ClientException(message = message)
    class NoAvailableCardProductCategoriesException(message: String) : ClientException(message = message)
  }
}
