package com.guavapay.paymentsdk.presentation.screens.mainpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayState
import com.guavapay.paymentsdk.network.services.OrderApi
import com.guavapay.paymentsdk.platform.algorithm.luhn
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class MainPageVM(
  private val lib: LibraryUnit,
  gateway: PaymentGatewayState
) : ViewModel() {

  data class State( // TODO: Will changed after BE side integration (order creation)
    val cardNumber: String = "",
    val expirationDate: String = "",
    val securityCode: String = "",
    val cardName: String = "",
    val saveCard: Boolean = false,
    val allowSaveCard: Boolean = true,
    val showCardName: Boolean = false,
    val isProcessing: Boolean = false,
    val cardNumberLoading: Boolean = false,
    val cardNumberError: String? = null,
    val expirationError: String? = null,
    val cardScheme: PaymentCardNetworks? = null,
    val maxCvcLength: Int = 4,
    val googlePayMethod: PaymentMethod? = null,
    val gateway: PaymentGatewayState
  )

  sealed class Effect { // TODO: Will changed after order creation integration.
    data class PaymentError(val message: String) : Effect()
    data class NavigateToResult(val success: Boolean) : Effect()
  }

  private val _state = MutableStateFlow(State(gateway = gateway))
  val state: StateFlow<State> = _state.asStateFlow()

  private val _effects = Channel<Effect>(BUFFERED, DROP_OLDEST)
  val effects: Flow<Effect> = _effects.receiveAsFlow()

  private var lastCardDetectedLength = 0

  init {
    _state
      .map { it.cardNumber.filter { char -> char.isDigit() } }
      .distinctUntilChanged()
      .filter { digits -> digits.length in 6..19 }
      .debounce(400)
      .onEach(::resolveCardNumber)
      .launchIn(viewModelScope)
  }

  fun updateCardNumber(cardNumber: String) {
    val digitsOnly = cardNumber.filter { it.isDigit() }.take(19)

    val currentState = _state.value
    _state.value = currentState.copy(
      cardNumber = digitsOnly,
      cardScheme = if (digitsOnly.length < 6) null else currentState.cardScheme,
      cardNumberLoading = digitsOnly.length in 6..19,
      cardNumberError = null,
      maxCvcLength = if (digitsOnly.length < 6) 4 else currentState.maxCvcLength
    )
  }

  fun onCardNumberFocusLost() {
    val digitsOnly = _state.value.cardNumber.filter { it.isDigit() }

    if (!luhn(digitsOnly)) {
      val errorMessage = lib.context.getString(R.string.error_invalid_card_number) // TODO: Use Text
      _state.value = _state.value.copy(
        cardNumberError = errorMessage
      )
    }
  }

  fun updateExpirationDate(expirationDate: String) {
    val digitsOnly = expirationDate.filter { it.isDigit() }.take(4)

    val currentState = _state.value
    _state.value = currentState.copy(expirationDate = digitsOnly, expirationError = null)
  }

  fun onExpirationDateFocusLost() {
    val expirationDate = _state.value.expirationDate

    if (expirationDate.length == 4) {
      val month = expirationDate.substring(0, 2).toIntOrNull()
      val year = expirationDate.substring(2, 4).toIntOrNull()

      if (month == null || year == null || month < 1 || month > 12) {
        _state.value = _state.value.copy(
          expirationError = lib.context.getString(R.string.error_invalid_card_number) // TODO: Use Text
        )
      }
    }
  }

  fun updateSecurityCode(securityCode: String) {
    val maxLength = _state.value.maxCvcLength
    val digitsOnly = securityCode.filter { it.isDigit() }.take(maxLength)

    val currentState = _state.value
    _state.value = currentState.copy(securityCode = digitsOnly)
  }

  fun updateCardName(name: String) {
    val trimmedName = name.take(32) // TODO: Need to ask to exactly restrictions about card name limitations and regexp.
    _state.value = _state.value.copy(cardName = trimmedName)
  }

  fun updateSaveCard(save: Boolean) {
    _state.value = _state.value.copy(saveCard = save, showCardName = save && _state.value.allowSaveCard)
  }

  val isPaymentButtonEnabled: Boolean
    get() = with(_state.value) {
      cardNumber.filter { it.isDigit() }.isNotBlank() &&
        expirationDate.isNotBlank() &&
        securityCode.isNotBlank() &&
        (!allowSaveCard || !saveCard || cardName.isNotBlank()) &&
        !isProcessing &&
        cardNumberError == null &&
        expirationError == null
    }

  fun performPayment() {
    viewModelScope.launch {
      _state.value = _state.value.copy(isProcessing = true)

      try {
        kotlinx.coroutines.delay(2000) // TODO: Eliminate delay after BE side integration.
        _effects.send(Effect.NavigateToResult(success = true))
      } catch (e: Exception) {
        _effects.send(Effect.PaymentError("Payment failed: ${e.message}"))
      } finally {
        _state.value = _state.value.copy(isProcessing = false)
      }
    }
  }

  private suspend fun resolveCardNumber(cardNumber: String) {
    if (cardNumber.length == lastCardDetectedLength) return

    try {
      _state.value = _state.value.copy(cardNumberLoading = true)

      val response = lib.network.services.order.getCardRangeData( // TODO: Use integrations
        request = OrderApi.Models.CardRangeRequest(
          rangeIncludes = cardNumber
        )
      )

      if (response.isSuccessful) {
        val responseBody = response.body()

        if (responseBody != null) {
          lastCardDetectedLength = cardNumber.length
          _state.value = _state.value.copy(
            cardNumberLoading = false,
            cardNumberError = null,
            cardScheme = responseBody.cardScheme,
            maxCvcLength = responseBody.cardScheme.cvc
          )
        } else {
          _state.value = _state.value.copy(cardNumberLoading = false, cardNumberError = null)
        }
      } else {
        _state.value = _state.value.copy(
          cardScheme = null,
          cardNumberLoading = false,
          cardNumberError = lib.context.getString(R.string.error_unable_to_identify_card), // TODO: Use Text
          maxCvcLength = 4
        )
      }
    } catch (_: Exception) { // todo: CEH (lib.coroutine.handlers)
      _state.value = _state.value.copy(
        cardScheme = null,
        cardNumberLoading = false,
        cardNumberError = lib.context.getString(R.string.error_unable_to_identify_card), // TODO: Use Text
        maxCvcLength = 4
      )
    }
  }
}