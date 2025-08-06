package com.guavapay.paymentsdk.gateway.banking

/**
 * A base class for all exceptions that can be thrown by the payment gateway.
 *
 * @property throwable The original exception that was thrown.
 * @property message The detail message string of this throwable.
 * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
 */
sealed class GatewayException(
  open val throwable: Throwable? = null,
  override val message: String = throwable?.message.toString()
) : Exception(message, throwable) {
  /**
   * Represents an unknown or unexpected exception that occurred within the gateway.
   *
   * @param throwable The original, underlying cause of this exception.
   * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
   */
  class UnknownException(throwable: Throwable) : GatewayException(throwable)

  /**
   * A base class for exceptions related to Google Pay operations.
   *
   * @property throwable The original exception that was thrown.
   * @property message The detail message string of this throwable.
   * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
   */
  sealed class GooglePayException(
    override val throwable: Throwable? = null,
    override val message: String = throwable?.message.toString()
  ) : GatewayException(throwable, message) {

    /**
     * Thrown when no payment data is returned from the Google Pay API.
     * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
     */
    class GooglePayNoPaymentDataException : GooglePayException()

    /**
     * Thrown when the Google Pay API returns an error.
     *
     * @param code The status code from the Google Pay API.
     * @param message The error message from the Google Pay API.
     * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
     */
    class GooglePayApiException(val code: Int?, message: String) : GooglePayException(message = message)

    /**
     * Represents an unknown or unexpected exception during a Google Pay operation.
     *
     * @param message The detail message string of this throwable.
     * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
     */
    class GooglePayUnknownException(message: String) : GooglePayException(message = message)

    /**
     * Thrown when an attempt is made to use Google Pay, but it is not ready.
     *
     * @param message The detail message string of this throwable.
     * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
     */
    class GooglePayNotReadyException(message: String) : GooglePayException(message = message)

    /**
     * Thrown when Google Pay has not been initialized before use.
     *
     * @param message The detail message string of this throwable.
     * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
     */
    class GooglePayNotInitializedException(message: String) : GooglePayException(message = message)
  }

  /**
   * A base class for exceptions originating from client-side errors.
   *
   * @property throwable The original exception that was thrown.
   * @property message The detail message string of this throwable.
   * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
   */
  sealed class ClientException(
    override val throwable: Throwable? = null,
    override val message: String = throwable?.message.toString()
  ) : GatewayException(throwable, message) {

    /**
     * Thrown when there are no available payment card schemes configured.
     *
     * @param message The detail message string of this throwable.
     * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
     */
    class NoAvailableCardSchemesException(message: String) : ClientException(message = message)

    /**
     * Thrown when there are no available payment card product categories configured.
     *
     * @param message The detail message string of this throwable.
     * @author Pavel Erokhin (MairwunNx / GuavaAgent007)
     */
    class NoAvailableCardProductCategoriesException(message: String) : ClientException(message = message)
  }
}
