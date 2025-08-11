@file:Suppress("OPT_IN_USAGE")

package com.guavapay.paymentsdk.presentation.screens.contact

import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType
import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.presentation.navigation.Route.ContactRoute
import com.guavapay.paymentsdk.presentation.platform.FIELD_DEBOUNCE_MS
import com.guavapay.paymentsdk.presentation.platform.FieldState
import com.guavapay.paymentsdk.presentation.platform.Text
import com.guavapay.paymentsdk.presentation.platform.collectDebounced
import com.guavapay.paymentsdk.presentation.platform.coroutinify
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.apache.commons.validator.routines.EmailValidator
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.math.max

internal class ContactVM(private val lib: LibraryUnit, private val handle: SavedStateHandle, private val route: ContactRoute) : ViewModel() {
  private val x by coroutinify(this, lib)

  private inline fun launch(
    context: CoroutineContext = EmptyCoroutineContext,
    handler: CoroutineExceptionHandler? = null,
    crossinline block: suspend CoroutineScope.() -> Unit
  ) = x.launch(context, handler, block)

  private val phones = PhoneNumberUtil.getInstance()
  private val emails = EmailValidator.getInstance()

  private val defaultCountryIso = route.countryIso ?: "GB"
  private val defaultCountryCode = iso2DialCode(defaultCountryIso) ?: "+44"

  private val _state = MutableStateFlow(
    State(
      email = handle["email"] ?: "",
      phone = handle["phone"] ?: "",
      countryCode = handle["countryCode"] ?: defaultCountryCode,
      countryIso = handle["countryIso"] ?: defaultCountryIso
    )
  )
  val state: StateFlow<State> = _state.asStateFlow()

  init {
    collectDebounced(
      scope = x.scope,
      source = state,
      selector = { it.email },
      block = ::finalizeEmail
    )

    launch {
      val phoneFlow = state.map { it.phone }.distinctUntilChanged().debounce(FIELD_DEBOUNCE_MS)
      val isoFlow = state.map { it.countryIso }.distinctUntilChanged()
      val ccFlow = state.map { it.countryCode }.distinctUntilChanged()
      combine(phoneFlow, isoFlow, ccFlow) { p, iso, cc -> Triple(p, iso, cc) }.collect { (p, iso, cc) -> finalizePhone(p, iso, cc) }
    }
  }

  internal fun iso2DialCode(iso: String): String? {
    val cc = phones.getCountryCodeForRegion(iso.uppercase(Locale.current.platformLocale))
    return if (cc > 0) "+$cc" else null
  }

  val handles = Handles() ; inner class Handles {
    fun email(email: String) {
      handle["email"] = email
      _state.update { it.copy(email = email) }
      val newState = when {
        email.isBlank() -> FieldState.EMPTY
        emails.isValid(email) -> FieldState.VALID
        else -> FieldState.INVALID_SILENT
      }
      _state.update { it.copy(emailState = newState, emailError = if (newState == FieldState.INVALID_SILENT) null else it.emailError, emailDirty = true) }
    }

    fun phone(phone: String) {
      val digits = phone.filter { it.isDigit() }
      val clipped = digits.take(maxNsnLen(_state.value.countryIso))
      handle["phone"] = clipped
      _state.update { it.copy(phone = clipped) }
      val newState = when {
        clipped.isBlank() -> FieldState.EMPTY
        isPhoneValid(clipped, _state.value.countryIso, _state.value.countryCode) -> FieldState.VALID
        else -> FieldState.INVALID_SILENT
      }
      _state.update { it.copy(phoneState = newState, phoneError = if (newState == FieldState.INVALID_SILENT) null else it.phoneError, phoneDirty = true) }
    }

    fun country(countryCode: String, countryIso: String) {
      handle["countryCode"] = countryCode
      handle["countryIso"] = countryIso
      _state.update { it.copy(countryCode = countryCode, countryIso = countryIso) }
      val p = _state.value.phone
      val newState = when {
        p.isBlank() -> FieldState.EMPTY
        isPhoneValid(p, countryIso, countryCode) -> FieldState.VALID
        else -> FieldState.INVALID_SILENT
      }
      _state.update { it.copy(phoneState = newState, phoneError = null, phoneDirty = newState != FieldState.EMPTY) }
    }

    fun onContinue() {
      val s = _state.value
      finalizeEmailNow(s.email)
      finalizePhoneNow(s.phone, s.countryIso, s.countryCode)
      val x = _state.value
      if (x.isValid) {
        val emailOrNull = x.email.takeIf(String::isNotBlank)
        val phoneOrNull = x.phone.takeIf(String::isNotBlank)?.let { "${x.countryCode}$it" }
        launch { route.callback(emailOrNull, phoneOrNull) }
      }
    }
  }

  private fun finalizeEmail(value: String) {
    val st = when {
      value.isBlank() -> FieldState.EMPTY
      emails.isValid(value) -> FieldState.VALID
      else -> FieldState.INVALID_VISIBLE
    }
    val err = if (st == FieldState.INVALID_VISIBLE) Text.Resource(R.string.error_invalid_email) else null
    _state.update { it.copy(emailState = st, emailError = err, emailDirty = false) }
  }

  private fun finalizePhone(value: String, iso: String, cc: String) {
    val st = when {
      value.isBlank() -> FieldState.EMPTY
      isPhoneValid(value, iso, cc) -> FieldState.VALID
      else -> FieldState.INVALID_VISIBLE
    }
    val err = if (st == FieldState.INVALID_VISIBLE) Text.Resource(R.string.error_invalid_phone_number) else null
    _state.update { it.copy(phoneState = st, phoneError = err, phoneDirty = false) }
  }

  private fun finalizeEmailNow(value: String) {
    val st = when {
      value.isBlank() -> FieldState.EMPTY
      emails.isValid(value) -> FieldState.VALID
      else -> FieldState.INVALID_VISIBLE
    }
    val err = if (st == FieldState.INVALID_VISIBLE) Text.Resource(R.string.error_invalid_email) else null
    _state.update { it.copy(emailState = st, emailError = err, emailDirty = false) }
  }

  private fun finalizePhoneNow(value: String, iso: String, cc: String) {
    val st = when {
      value.isBlank() -> FieldState.EMPTY
      isPhoneValid(value, iso, cc) -> FieldState.VALID
      else -> FieldState.INVALID_VISIBLE
    }
    val err = if (st == FieldState.INVALID_VISIBLE) Text.Resource(R.string.error_invalid_phone_number) else null
    _state.update { it.copy(phoneState = st, phoneError = err, phoneDirty = false) }
  }

  private fun isPhoneValid(nsn: String, iso: String, cc: String) = try {
    val parsed = phones.parse("$cc$nsn", iso)
    phones.isValidNumber(parsed)
  } catch (_: NumberParseException) { false }

  private fun maxNsnLen(iso: String): Int {
    fun len(type: PhoneNumberType) = phones.getExampleNumberForType(iso, type)?.let { phones.getNationalSignificantNumber(it).length } ?: 0
    val m = len(PhoneNumberType.MOBILE)
    val f = len(PhoneNumberType.FIXED_LINE)
    return max(m, f).takeIf { it > 0 } ?: 15
  }

  data class State(
    val email: String = "",
    val phone: String = "",
    val countryCode: String = "+44",
    val countryIso: String = "GB",
    val emailError: Text? = null,
    val phoneError: Text? = null,
    val emailState: FieldState = FieldState.EMPTY,
    val phoneState: FieldState = FieldState.EMPTY,
    val emailDirty: Boolean = false,
    val phoneDirty: Boolean = false,
  ) {
    val isValid: Boolean get() {
      val anyFilled = email.isNotBlank() || phone.isNotBlank()
      val emailOk = email.isBlank() || emailState == FieldState.VALID
      val phoneOk = phone.isBlank() || phoneState == FieldState.VALID
      return anyFilled && emailOk && phoneOk
    }
  }
}