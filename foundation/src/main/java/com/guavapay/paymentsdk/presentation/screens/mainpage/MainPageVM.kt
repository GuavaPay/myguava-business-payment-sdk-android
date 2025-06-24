@file:Suppress("OPT_IN_USAGE")

package com.guavapay.paymentsdk.presentation.screens.mainpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks
import com.guavapay.paymentsdk.gateway.banking.PaymentKind
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod.Card
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod.GooglePay
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod.SavedCard
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayState
import com.guavapay.paymentsdk.integrations.IntegrationException
import com.guavapay.paymentsdk.integrations.remote.RemoteCardRangeData
import com.guavapay.paymentsdk.platform.algorithm.luhn
import com.guavapay.paymentsdk.platform.coroutines.CompositeExceptionHandler
import com.guavapay.paymentsdk.platform.coroutines.ExceptionHandler
import com.guavapay.paymentsdk.presentation.platform.Text
import com.guavapay.paymentsdk.presentation.platform.Text.Plain
import com.guavapay.paymentsdk.presentation.platform.locale
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainPageVM.Effect.PaymentError
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainPageVM.State.ExternalState
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainPageVM.State.ExternalState.FlagsState
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.delay
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class MainPageVM(private val lib: LibraryUnit, private val gateway: PaymentGatewayState) : ViewModel() {
  private val _state = MutableStateFlow(State())
  val state: StateFlow<State> = _state.asStateFlow()

  private val _effects = Channel<Effect>(BUFFERED, DROP_OLDEST)
  val effects: Flow<Effect> = _effects.receiveAsFlow()

  private val instruments = Instruments(); inner class Instruments {
    val card = gateway.instruments.instrument<Card>()
    val saved = gateway.instruments.instrument<SavedCard>()
    val gpay = gateway.instruments.instrument<GooglePay>()
  }

  init {
    initial()
    resolvers()
  }

  private fun resolvers() {
    _state
      .map { it.external.fields.pan.filter(Char::isDigit) }
      .distinctUntilChanged()
      .filter { digits -> digits.length in 6..19 }
      .debounce(300)
      .onEach(::resolvePan)
      .launchIn(viewModelScope)
  }

  private fun initial() {
    _state.update {
      it.copy(
        external = ExternalState(
          flags = FlagsState(
            save = instruments.saved != null && instruments.card != null && instruments.card.flags.allowSaveCard,
            gpay = instruments.gpay != null && instruments.gpay.networks.count { it != PaymentCardNetworks.UNIONPAY } > 0,
          ),
          paytext = Plain(gateway.amount.format(lib.context.locale())),
          paykind = gateway.kind,
          networks = instruments.card?.networks.orEmpty().toList(),
        )
      )
    }
  }

  private suspend fun resolvePan(pan: String) {
    _state.update { state ->
      state.copy(
        external = state.external.copy(
          fields = state.external.fields.copy(panBusy = true)
        )
      )
    }

    runCatching {
      val response = RemoteCardRangeData(lib, pan)
      if (response.cardScheme == null && response.product == null) {
        _state.update { state ->
          state.copy(
            external = state.external.copy(fields = state.external.fields.copy(panBusy = false))
          )
        }
      } else {
        _state.update { state ->
          state.copy(
            external = state.external.copy(
              fields = state.external.fields.copy(
                panBusy = false,
                panError = null,
                panNetwork = response.cardScheme,
                cvvLength = response.cardScheme!!.cvc
              )
            )
          )
        }
      }
    }.onFailure { e ->
      if (e is IntegrationException.ClientError) {
        _state.update { state ->
          state.copy(
            external = state.external.copy(
              fields = state.external.fields.copy(
                panBusy = false,
                panNetwork = null,
                panError = Text.Resource(R.string.error_unable_to_identify_card),
                cvvLength = 4
              )
            )
          )
        }

        return@onFailure
      }

      viewModelScope.launch {
        _effects.send(Effect.AbortDueError)
      }
    }
  }

  val handles = Handles() ; inner class Handles {
    fun pay() {
      if (!isEligibleToPay) return

      val state = state.value.external

      val contact = state.contact
      if (contact == null || contact.email.isBlank() || contact.phone.isBlank()) {
        viewModelScope.launch {
          _effects.send(Effect.RequiredContacts)
        }
        return
      }

      viewModelScope.launch(lib.coroutine.elements.named + CompositeExceptionHandler(error.logcat, error.metrica, ExceptionHandler(error::payment))) {
        _state.update { state -> state.copy(external = state.external.copy(busy = true)) }
        delay(2000)
        _effects.send(Effect.NavigateToResult(success = true))
        _state.update { state -> state.copy(external = state.external.copy(busy = false)) }
      }
    }

    fun gpay(result: PaymentResult) = Unit

    fun pan(pan: String) {
      val digitsOnly = pan.filter(Char::isDigit).take(19)

      _state.update { state ->
        state.copy(
          external = state.external.copy(
            fields = state.external.fields.copy(
              pan = digitsOnly,
              panNetwork = if (digitsOnly.length < 6) null else state.external.fields.panNetwork,
              panBusy = digitsOnly.length in 6..19,
              panError = null,
              cvvLength = if (digitsOnly.length < 6) 4 else state.external.fields.cvvLength
            )
          )
        )
      }
    }

    fun exp(exp: String) {
      val digitsOnly = exp.filter(Char::isDigit).take(4)

      _state.update { state ->
        state.copy(
          external = state.external.copy(
            fields = state.external.fields.copy(
              exp = digitsOnly,
              expError = null
            )
          )
        )
      }
    }

    fun cvv(cvv: String) {
      val maxLength = _state.value.external.fields.cvvLength
      val digitsOnly = cvv.filter(Char::isDigit).take(maxLength)

      _state.update { state ->
        state.copy(
          external = state.external.copy(
            fields = state.external.fields.copy(
              cvv = digitsOnly,
              cvvError = null
            )
          )
        )
      }
    }

    fun cn(cn: String) {
      val cn = cn.take(32).trim()

      _state.update { state ->
        state.copy(
          external = state.external.copy(
            fields = state.external.fields.copy(
              cn = cn,
              cnError = null
            )
          )
        )
      }
    }

    fun toggleSave(save: Boolean) {
      _state.update { state ->
        state.copy(
          external = state.external.copy(
            saving = save,
          )
        )
      }
    }

    fun panFocusLost() {
      val digitsOnly = _state.value.external.fields.pan.filter(Char::isDigit)

      if (!luhn(digitsOnly)) {
        _state.update { state ->
          state.copy(
            external = state.external.copy(
              fields = state.external.fields.copy(
                panError = Text.Resource(R.string.error_invalid_card_number)
              )
            )
          )
        }
      }

      cvvFocusLost()
    }

    fun expFocusLost() {
      val exp = _state.value.external.fields.exp

      if (exp.length == 4) {
        val month = exp.substring(0, 2).toIntOrNull()
        val year = exp.substring(2, 4).toIntOrNull()

        if (month == null || year == null || month < 1 || month > 12) {
          _state.update { state ->
            state.copy(
              external = state.external.copy(
                fields = state.external.fields.copy(
                  expError = Text.Resource(R.string.error_invalid_exp_number)
                )
              )
            )
          }
        }
      }
    }

    fun cvvFocusLost() {
      val cvv = _state.value.external.fields.cvv
      val requiredLength = _state.value.external.fields.cvvLength

      if (cvv.isNotEmpty() && cvv.length < requiredLength) {
        _state.update { state ->
          state.copy(external = state.external.copy(fields = state.external.fields.copy(cvvError = Text.Resource(R.string.error_invalid_cvv))))
        }
      }
    }

    fun cnFocusLost() {
      val cn = _state.value.external.fields.cn
      if (cn.length > 32) {
        _state.update { state ->
          state.copy(external = state.external.copy(fields = state.external.fields.copy(cnError = Text.Resource(R.string.error_invalid_card_name))))
        }
      }
    }

    val isEligibleToPay: Boolean
      get() = with(state.value.external) {
        val fields = fields
        !busy && !fields.panBusy && contact?.busy == false && fields.panError == null && fields.expError == null && fields.cvvError == null && fields.cvv.isNotBlank() && (!flags.save || fields.cnError == null)
      }
  }

  private val error = Error() ; inner class Error() {
    val logcat = lib.coroutine.handlers.logcat
    val metrica = lib.coroutine.handlers.metrica

    fun payment(t: Throwable) {
      viewModelScope.launch {
        _effects.send(PaymentError(Plain("Payment failed: ${t.message}")))
      }
    }
  }

  data class State(private val internal: InternalState = InternalState(), val external: ExternalState = ExternalState()) {
    data class InternalState(val placebo: Boolean = false)

    data class ExternalState(
      val fields: FieldsState = FieldsState(),
      val flags: FlagsState = FlagsState(),
      val contact: ContactState? = null,
      val busy: Boolean = false,
      val saving: Boolean = false,
      val pay: Boolean = false,
      val paytext: Text? = null,
      val paykind: PaymentKind? = null,
      val networks: List<PaymentCardNetworks> = emptyList(),
    ) {
      data class FlagsState(
        val save: Boolean = false,
        val gpay: Boolean = false,
      )

      data class FieldsState(
        val pan: String = "",
        val exp: String = "",
        val cvv: String = "",
        val cn: String = "",

        val panError: Text? = null,
        val expError: Text? = null,
        val cvvError: Text? = null,
        val cnError: Text? = null,

        val panNetwork: PaymentCardNetworks? = null,
        val panBusy: Boolean = false,
        val cvvLength: Int = 4,
      )

      data class ContactState(
        val email: String = "",
        val phone: String = "",
        val busy: Boolean = false,
      )
    }
  }

  sealed interface Effect {
    data class PaymentError(val message: Text) : Effect
    data class NavigateToResult(val success: Boolean) : Effect
    data object RequiredContacts : Effect
    data object AbortDueError : Effect
  }
}