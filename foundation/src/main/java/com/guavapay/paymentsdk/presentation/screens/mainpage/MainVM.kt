@file:Suppress("OPT_IN_USAGE")

package com.guavapay.paymentsdk.presentation.screens.mainpage

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.guavapay.myguava.business.myguava3ds2.init.ui.GUiCustomization
import com.guavapay.myguava.business.myguava3ds2.service.GuavaThreeDs2ServiceImpl
import com.guavapay.myguava.business.myguava3ds2.transaction.ChallengeParameters
import com.guavapay.myguava.business.myguava3ds2.transaction.InitChallengeRepositoryFactory
import com.guavapay.myguava.business.myguava3ds2.transaction.SdkTransactionId
import com.guavapay.myguava.business.myguava3ds2.transaction.Transaction
import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.gateway.banking.GatewayException
import com.guavapay.paymentsdk.gateway.banking.GatewayException.ClientException.NoAvailableCardProductCategoriesException
import com.guavapay.paymentsdk.gateway.banking.GatewayException.ClientException.NoAvailableCardSchemesException
import com.guavapay.paymentsdk.gateway.banking.GatewayException.UnknownException
import com.guavapay.paymentsdk.gateway.banking.PaymentCardCategory
import com.guavapay.paymentsdk.gateway.banking.PaymentCardScheme
import com.guavapay.paymentsdk.gateway.banking.PaymentKind
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod.PaymentCard
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod.GooglePay
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod.PaymentCardBinding
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.gateway.banking.PaymentResult.Companion.toResult
import com.guavapay.paymentsdk.gateway.vendors.googlepay.GPayResult
import com.guavapay.paymentsdk.integrations.IntegrationException
import com.guavapay.paymentsdk.integrations.remote.RemoteCardRangeData
import com.guavapay.paymentsdk.integrations.remote.RemoteDeleteBinding
import com.guavapay.paymentsdk.integrations.remote.RemoteEditBinding
import com.guavapay.paymentsdk.integrations.remote.RemoteExecutePayment
import com.guavapay.paymentsdk.integrations.remote.RemoteGetBindings
import com.guavapay.paymentsdk.integrations.remote.RemoteGooglePayContext
import com.guavapay.paymentsdk.integrations.remote.RemoteOrderSubscription
import com.guavapay.paymentsdk.logging.d
import com.guavapay.paymentsdk.logging.e
import com.guavapay.paymentsdk.logging.w
import com.guavapay.paymentsdk.network.services.OrderApi
import com.guavapay.paymentsdk.network.services.OrderApi.Models.GetOrderResponse
import com.guavapay.paymentsdk.platform.algorithm.luhn
import com.guavapay.paymentsdk.platform.arrays.intersectByName
import com.guavapay.paymentsdk.platform.coroutines.ExceptionHandler
import com.guavapay.paymentsdk.presentation.navigation.NavigationEvents
import com.guavapay.paymentsdk.presentation.navigation.NavigationEvents.Companion.key
import com.guavapay.paymentsdk.presentation.platform.FieldState
import com.guavapay.paymentsdk.presentation.platform.Text
import com.guavapay.paymentsdk.presentation.platform.basy
import com.guavapay.paymentsdk.presentation.platform.collectDebounced
import com.guavapay.paymentsdk.presentation.platform.currencify
import com.guavapay.paymentsdk.presentation.platform.retrow
import com.guavapay.paymentsdk.presentation.screens.mainpage.threeds.ThreedsInterconnect
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException

internal class MainVM(private val lib: LibraryUnit, private val handle: SavedStateHandle) : ViewModel() {
  private val x by basy(lib)

  private inline fun launch(
    context: CoroutineContext = EmptyCoroutineContext,
    handler: CoroutineExceptionHandler? = null,
    crossinline block: suspend CoroutineScope.() -> Unit
  ) = x.launch(context, handler, block)

  private val threeDs = ThreedsInterconnect(lib) { uiCustomization }

  data class State(
    val mode: Mode = Mode.NewCard,
    val fields: Fields = Fields(),
    val contact: Contact? = null,
    val busy: Boolean = false,
    val saving: Boolean = false,
    val payText: Text? = null,
    val payKind: PaymentKind? = null,
    val networks: List<PaymentCardScheme> = emptyList(),
    val gpay: GPay? = null,
    val saved: Saved? = null,
  ) {
    enum class Mode { SavedCard, NewCard }

    data class Fields(
      val pan: String = "",
      val exp: String = "",
      val cvv: String = "",
      val cn: String = "",
      val ch: String = "",

      val panError: Text? = null,
      val expError: Text? = null,
      val cvvError: Text? = null,
      val cnError: Text? = null,
      val chError: Text? = null,

      val panNetwork: PaymentCardScheme? = null,
      val panCategory: PaymentCardCategory? = null,
      val panBusy: Boolean = false,
      val cvvLength: Int = 4,
      val cardHolderNameAvailable: Boolean = false,

      val chState: FieldState = FieldState.EMPTY,
      val chDirty: Boolean = false,
    )

    data class Contact(
      val email: String = "",
      val maskedEmail: String = "",
      val phone: String = "",
      val maskedPhone: String = "",
      val busy: Boolean = false,
    )

    data class GPay(val available: Boolean = false)

    data class Saved(
      val available: Boolean = false,
      val savingAvailable: Boolean = false,
      val cards: List<SavedCard> = emptyList(),
      val selectedCardId: String? = null,
      val cvvInput: String = "",
      val cvvError: Text? = null,
      val isLoadingCards: Boolean = false
    )

    data class SavedCard(
      val id: String,
      val maskedPan: String,
      val cardName: String,
      val expiryDate: String,
      val scheme: PaymentCardScheme,
      val category: PaymentCardCategory,
      val isAvailable: Boolean,
      val cvvLength: Int
    )
  }

  data class Internal(
    val order: OrderApi.Models.Order? = null,
    val gpayCtx: OrderApi.Models.GooglePayContext? = null,
    val allowedMethods: Set<String> = emptySet(),
    val allowedSchemes: List<PaymentCardScheme> = emptyList(),
    val allowedCategories: Set<PaymentCardCategory> = emptySet(),
    val allowedCurrencies: List<String> = emptyList()
  )

  sealed interface Effect {
    data class ShowError(val message: Text) : Effect
    data class AskContacts(val countryIso: String? = null, val requestKey: String) : Effect
    data class AbortError(val throwable: GatewayException? = null) : Effect
    data object AbortGuard : Effect
    data class Finish(val result: PaymentResult) : Effect
    data object FocusPan : Effect
    data object FocusExp : Effect
    data object FocusCvv : Effect
    data object FocusCardholder : Effect
    data object HideKeyboard : Effect
    data class ConfirmDeleteCard(val cardId: String, val cardName: String, val requestKey: String) : Effect
    data class EditCard(val cardId: String, val cardName: String, val requestKey: String) : Effect
    data class Require3ds(val params: ChallengeParameters, val tx: Transaction) : Effect
  }

  private val payload = lib.state.payload()

  private val instruments = Instruments() ; private inner class Instruments {
    val card = payload.availablePaymentMethods.filterIsInstance<PaymentCard>().firstOrNull()
    val saved = payload.availablePaymentMethods.filterIsInstance<PaymentCardBinding>().firstOrNull()
    val gpay = payload.availablePaymentMethods.filterIsInstance<GooglePay>().firstOrNull()
  }

  var internal = Internal()

  private val _state = MutableStateFlow(State())
  val state: StateFlow<State> = _state.asStateFlow()

  private val _effects = Channel<Effect>(BUFFERED, DROP_OLDEST)
  val effects: Flow<Effect> = _effects.receiveAsFlow()

  lateinit var uiCustomization: GUiCustomization
  private val threeds = GuavaThreeDs2ServiceImpl(lib.context, enableLogging = true, workContext = lib.coroutine.dispatchers.common)

  init { bind() }

  private fun bind() = with(x) {
    updateFields { it.copy(cardHolderNameAvailable = instruments.card?.flags?.allowCardHolderName == true) }

    RemoteOrderSubscription(lib)
      .onStart {
        busy()
        lib.metrica.breadcrumb("Fetch-Started", "Sdk Order", "state")
      }
      .distinctUntilChanged()
      .onEach {
        d("Order stream: status=${it.order.status} id=${it.order.id}")
        onOrder(it)
        lib.metrica.breadcrumb("Fetch-Finished", "Sdk Order", "state", data = mapOf("status" to it.order.status, "order_id" to it.order.id))
        unbusy()
      }
      .catch {
        unbusy()
        if (it !is CancellationException) {
          with(UnknownException(it)) {
            lib.metrica.breadcrumb("Fetch-Error", "Sdk Order", "error", data = mapOf("error_type" to javaClass.simpleName, "error_msg" to (message ?: "")))
            fatal(this)
            throw UnknownException(this)
          }
        }
      }
      .launch()

    state
      .onStart {
        lib.metrica.breadcrumb("Pan-Initiated", "Sdk Validation", "state")
      }
      .map { it.fields.pan.filter(Char::isDigit) }
      .distinctUntilChanged()
      .filter { it.length in PAN_MIN..PAN_MAX }
      .debounce(PAN_DEBOUNCE_MS)
      .onEach { busyPan() }
      .onEach(::onPanRange)
      .onEach { unbusyPan() }
      .catch {
        unbusyPan()
        if (it !is CancellationException) {
          if (it is IntegrationException.ClientError) {
            w("Card range client error code=${it.code}")
            lib.metrica.breadcrumb("Pan-Error", "Sdk Validation", "error", data = mapOf("code" to it.code))
            fieldError()
          } else {
            with(UnknownException(it)) {
              lib.metrica.breadcrumb("Pan-Error", "Sdk Validation", "error", data = mapOf("error_type" to javaClass.simpleName, "error_msg" to (message ?: "")))
              fatal(this)
              throw UnknownException(this)
            }
          }
        }
      }
      .launch()

    collectDebounced(
      scope = scope,
      source = state,
      selector = { it.fields.ch },
      block = ::finalizeCh
    )
  }

  private inline fun update(block: (State) -> State) {
    _state.update(block)
  }

  private inline fun updateExternal(block: (State) -> State) = update(block)
  private inline fun updateFields(block: (State.Fields) -> State.Fields) = update { it.copy(fields = block(it.fields)) }

  private fun busy() = updateExternal { it.copy(busy = true) }
  private fun unbusy() = updateExternal { it.copy(busy = false) }
  private fun busyPan() = updateFields { it.copy(panBusy = true) }
  private fun unbusyPan() = updateFields { it.copy(panBusy = false) }

  private fun SavedStateHandle.getMode() =
    get<String>(KEY_MODE)?.let run@{
      return@run when (it) {
        "SavedCard" -> State.Mode.SavedCard
        "NewCard" -> State.Mode.NewCard
        else -> null
      }
    }

  private fun SavedStateHandle.setMode(mode: State.Mode) {
    set(
      KEY_MODE, when (mode) {
        State.Mode.SavedCard -> "SavedCard"
        State.Mode.NewCard -> "NewCard"
      }
    )
  }

  private suspend fun fatal(err: GatewayException? = null) {
    _effects.send(Effect.AbortError(err))
  }

  private fun fieldError() = updateFields {
    it.copy(
      panBusy = false,
      panNetwork = null,
      panError = Text.Resource(R.string.error_unable_to_identify_card),
      cvvLength = FALLBACK_CVV
    )
  }

  private suspend fun loadSavedCards(
    order: OrderApi.Models.Order,
    schemes: List<PaymentCardScheme>,
    categories: Set<PaymentCardCategory>
  ): List<State.SavedCard>? {
    if (instruments.saved == null) return null
    if (order.payer?.id == null) return null

    return try {
      val resp = RemoteGetBindings(lib)
      val ui = resp.data.map { b ->
        val scheme = PaymentCardScheme.valueOf(b.cardData.cardScheme)
        val cat = b.product?.category?.let { PaymentCardCategory.valueOf(it) } ?: error("Unknown card category in binding id=${b.id}")
        State.SavedCard(
          id = b.id,
          maskedPan = "*${b.cardData.maskedPan.takeLast(4)}",
          cardName = b.name,
          expiryDate = b.cardData.expiryDate,
          scheme = scheme,
          category = cat,
          isAvailable = scheme in schemes && cat in categories && b.activity,
          cvvLength = scheme.cvc
        )
      }
      updateExternal { it.copy(saved = it.saved?.copy(isLoadingCards = false)) }
      ui
    } catch (t: Exception) {
      e("Failed to load saved cards: ${t.message}", t)
      updateExternal { it.copy(saved = it.saved?.copy(isLoadingCards = false)) }
      null
    }
  }

  private suspend fun onOrder(resp: GetOrderResponse) {
    when (resp.order.status) {
      S_CREATED -> Unit
      S_PAID -> return _effects.send(Effect.Finish(PaymentResult.Success(resp.payment?.toResult(), resp.order.toResult())))
      S_DECLINED -> return _effects.send(Effect.Finish(PaymentResult.Unsuccess(resp.payment?.toResult(), resp.order.toResult())))
      S_PARTIALLY_REFUNDED -> return _effects.send(Effect.Finish(PaymentResult.Success(resp.payment?.toResult(), resp.order.toResult())))
      S_REFUNDED -> return _effects.send(Effect.Finish(PaymentResult.Success(resp.payment?.toResult(), resp.order.toResult())))
      S_CANCELLED -> return _effects.send(Effect.Finish(PaymentResult.Unsuccess(resp.payment?.toResult(), resp.order.toResult())))
      S_EXPIRED -> return _effects.send(Effect.Finish(PaymentResult.Unsuccess(resp.payment?.toResult(), resp.order.toResult())))
      S_RECURRENCE_ACTIVE -> return _effects.send(Effect.Finish(PaymentResult.Success(resp.payment?.toResult(), resp.order.toResult())))
      S_RECURRENCE_CLOSED -> return _effects.send(Effect.Finish(PaymentResult.Success(resp.payment?.toResult(), resp.order.toResult())))
      else -> return _effects.send(Effect.Finish(PaymentResult.Error(IllegalStateException("Unprocessable order status: ${resp.order.status}"))))
    }

    val amount = currencify(resp.order.totalAmount.baseUnits, resp.order.totalAmount.currency, payload.locale())

    val sdkMethods = payload.availablePaymentMethods.mapNotNull {
      when (it) {
        is PaymentCard -> TYPE_CARD
        is GooglePay -> TYPE_GPAY
        is PaymentCardBinding -> TYPE_BINDING
        else -> null
      }
    }.toSet()

    val allowedMethods = resp.order.availablePaymentMethods.intersect(sdkMethods)

    val sdkSchemes = payload.availableCardSchemes.map { it.name }.toSet()
    val allowedSchemes = resp.order.availableCardSchemes.intersectByName(sdkSchemes).toList().also {
      if (it.isEmpty()) return _effects.send(Effect.AbortError(NoAvailableCardSchemesException("No available card schemes")))
    }

    val sdkCats = payload.availableCardProductCategories.map { it.name }.toSet()
    val allowedCats = resp.order.availableCardProductCategories.intersectByName(sdkCats).also {
      if (it.isEmpty()) return _effects.send(Effect.AbortError(NoAvailableCardProductCategoriesException("No available card product categories")))
    }

    val cards = loadSavedCards(resp.order, allowedSchemes, allowedCats)
    val hasSaved = cards?.any { it.isAvailable } == true

    val gpayCtx = if (TYPE_GPAY in allowedMethods && instruments.gpay != null) {
      try {
        val ctx = RemoteGooglePayContext(lib).context
        val names = allowedSchemes.map(PaymentCardScheme::name).toSet()
        ctx.copy(allowedCardSchemes = ctx.allowedCardSchemes.intersect(names).toList())
      } catch (t: Exception) {
        w("Failed to load Google Pay context: ${t.message}")
        null
      }
    } else null

    val contact = resp.order.payer?.let { p ->
      State.Contact(
        email = "",
        phone = "",
        maskedEmail = p.maskedContactEmail ?: p.contactEmail ?: "",
        maskedPhone = p.maskedContactPhone?.formatted ?: p.contactPhone?.fullNumber ?: ""
      )
    }

    internal = internal.copy(
      order = resp.order.copy(availablePaymentMethods = resp.order.availablePaymentMethods),
      gpayCtx = gpayCtx,
      allowedMethods = allowedMethods,
      allowedSchemes = allowedSchemes,
      allowedCategories = allowedCats,
      allowedCurrencies = resp.order.availablePaymentCurrencies
    )

    val savedMode = handle.getMode()
    val bindingAllowed = TYPE_BINDING in allowedMethods && instruments.saved != null
    val computedMode = if (bindingAllowed && hasSaved) State.Mode.SavedCard else State.Mode.NewCard
    val mode = when (savedMode) {
      State.Mode.SavedCard -> if (hasSaved) State.Mode.SavedCard else State.Mode.NewCard
      State.Mode.NewCard -> State.Mode.NewCard
      null -> computedMode
    }
    handle.setMode(mode)

    update {
      it.copy(
        mode = mode,
        payText = Text.Plain(amount),
        payKind = payload.kind,
        networks = allowedSchemes,
        contact = contact,
        saved = State.Saved(
          available = bindingAllowed && !cards.isNullOrEmpty(),
          savingAvailable = TYPE_BINDING in allowedMethods && (resp.order.payer?.id != null),
          cards = cards ?: emptyList()
        ),
        gpay = State.GPay(
          available = TYPE_GPAY in allowedMethods &&
            instruments.gpay != null &&
            gpayCtx != null &&
            internal.allowedCurrencies.isNotEmpty()
        )
      )
    }

    d("Order resolved: mode=$mode, gpay=${state.value.gpay?.available}, saved=${state.value.saved?.cards?.size}")
  }

  private suspend fun onPanRange(pan: String) {
    val data = RemoteCardRangeData(lib, pan)
    if (data.cardScheme != null || data.product != null) {
      updateFields {
        it.copy(
          panBusy = false,
          panNetwork = data.cardScheme,
          cvvLength = data.cardScheme!!.cvc,
          panCategory = data.product?.category
        )
      }

      lib.metrica.breadcrumb("Pan-Resolved", "Sdk Validation", "state", data = mapOf(
          "scheme" to (data.cardScheme?.name ?: ""),
          "category" to (data.product?.category?.name ?: ""),
          "pan_len" to pan.length
        )
      )
    }
  }

  private fun finalizeCh(value: String) {
    val st = when {
      value.isBlank() -> FieldState.EMPTY
      isChValid(value) -> FieldState.VALID
      else -> FieldState.INVALID_VISIBLE
    }
    val err = if (st == FieldState.INVALID_VISIBLE) Text.Resource(R.string.error_cardholder_name_invalid_chars) else null
    updateFields { it.copy(chState = st, chError = err, chDirty = false) }
  }

  private fun isChValid(v: String): Boolean {
    if (v.isBlank() || v.length > 25) return false
    if (v.first().isWhitespace() || v.last().isWhitespace()) return false
    val parts = v.split(' ')
    if (parts.size !in 1..2) return false
    return parts.all { it.isNotEmpty() && it.all { ch -> ch in 'A'..'Z' } }
  }

  val handles = Handles() ; inner class Handles {
    fun changeContactInfo() {
      launch {
        val result = with(key()) {
          _effects.send(Effect.AskContacts(countryIso = internal.order?.payer?.address?.country, requestKey = this))
          lib.navigation.await<NavigationEvents.Event.ContactResult>(this)
        }

        update {
          it.copy(
            contact = it.contact?.let { c ->
              c.copy(
                email = result.email ?: c.email,
                maskedEmail = result.email ?: c.maskedEmail,
                phone = result.phone ?: c.phone,
                maskedPhone = result.phone ?: c.maskedPhone
              )
            }
          )
        }
      }
    }

    fun pay() {
      if (!isEligibleToPay) return
      val contact = state.value.contact
      if (contact == null || (contact.maskedEmail.isBlank() && contact.maskedPhone.isBlank())) {
        launch {
          val result = with(key()) {
            _effects.send(Effect.AskContacts(countryIso = internal.order?.payer?.address?.country, requestKey = this))
            lib.navigation.await<NavigationEvents.Event.ContactResult>(this)
          }

          update {
            it.copy(
              contact = it.contact?.let { c ->
                c.copy(
                  email = result.email ?: c.email,
                  maskedEmail = result.email ?: c.maskedEmail,
                  phone = result.phone ?: c.phone,
                  maskedPhone = result.phone ?: c.maskedPhone
                )
              }
            )
          }
        }
        return
      }

      lib.metrica.breadcrumb("Pay-Initiated", "Sdk Payment", "action", data = mapOf("method" to "card"))

      launch(ExceptionHandler { e -> _effects.trySend(Effect.AbortError(UnknownException(e))) }) {
        val s = state.value
        executeCardPayment(
          paymentMethod = OrderApi.Models.PaymentMethod(
            type = TYPE_CARD,
            pan = s.fields.pan,
            cvv2 = s.fields.cvv,
            expiryDate = convertToYYMM(s.fields.exp),
            cardholderName = s.fields.ch.takeIf(String::isNotBlank),
          ),
          bindingCreationIsNeeded = s.saving,
          bindingName = if (s.saving && s.fields.cn.isNotBlank()) s.fields.cn else null
        )
      }
    }

    fun payViaSavedCard() {
      if (!isEligibleToPay) return
      val s = state.value
      val cardId = s.saved?.selectedCardId ?: return
      val cardCvv = s.saved.cvvInput

      lib.metrica.breadcrumb("Pay-Initiated", "Sdk Payment", "action", data = mapOf("method" to "saved_card"))

      launch(ExceptionHandler { e -> _effects.trySend(Effect.AbortError(UnknownException(e))) }) {
        executeCardPayment(
          paymentMethod = OrderApi.Models.PaymentMethod(type = TYPE_BINDING, bindingId = cardId, cvv2 = cardCvv),
          bindingCreationIsNeeded = s.saving,
          bindingName = if (s.saving) s.fields.cn.takeIf(String::isNotBlank) else null
        )
      }
    }

    fun gpay(result: GPayResult) {
      lib.metrica.breadcrumb("Pay-Initiated", "Sdk Payment", "action", data = mapOf("method" to "google_pay", "result" to result::class.simpleName))

      launch(ExceptionHandler { e -> _effects.trySend(Effect.AbortError(UnknownException(e))) }) {
        when (result) {
          is GPayResult.Success -> executeGooglePayPayment(result.data, bindingCreationIsNeeded = state.value.saving)
          is GPayResult.Failed -> _effects.send(Effect.ShowError(Text.Plain(result.throwable?.message ?: "Google Pay error")))
          is GPayResult.Canceled -> Unit
        }
      }
    }

    fun getChallengeRepositoryFactory(
      application: Application = this@MainVM.lib.context.applicationContext as Application,
      isLiveMode: Boolean = false,
      sdkTransactionId: SdkTransactionId = SdkTransactionId.create(),
      uiCustomization: GUiCustomization = this@MainVM.uiCustomization,
      coroutineContext: CoroutineContext = lib.coroutine.dispatchers.common
    ) =
      InitChallengeRepositoryFactory(
        application, isLiveMode, sdkTransactionId, uiCustomization,
        rootCerts = emptyList(), enableLogging = false, coroutineContext
      ).create()

    suspend fun executeCardPayment(paymentMethod: OrderApi.Models.PaymentMethod, bindingCreationIsNeeded: Boolean = false, bindingName: String? = null) {
      try {
        busy()

        lib.metrica.breadcrumb("Payment-Execute-Initiated", "Sdk Payment", "action", data = mapOf("method" to paymentMethod.type, "create_binding" to bindingCreationIsNeeded))

        val order = internal.order ?: throw UnknownException(IllegalStateException("Order data not available"))
        val orderId = order.id

        val contact = state.value.contact
        val email = contact?.email?.takeIf(String::isNotBlank)
        val phoneParsed = contact?.phone?.takeIf(String::isNotBlank)?.let(::parseContactPhone)
        val payer = if (email == null && phoneParsed == null) null else
          OrderApi.Models.Payer(
            contactEmail = email,
            contactPhone = phoneParsed?.let { OrderApi.Models.Phone(it.countryCode, it.nationalNumber, it.fullNumber) }
          )

        val req = OrderApi.Models.ExecutePaymentRequest(
          paymentMethod = paymentMethod,
          deviceData = createDeviceData(),
          bindingCreationIsNeeded = bindingCreationIsNeeded,
          bindingName = bindingName,
          payer = payer
        )

        val resp = RemoteExecutePayment(lib, orderId, req)
        threeDs.handleIfNeeded(resp, orderId)?.let { _effects.send(it) }
      } catch (t: Exception) {
        lib.metrica.breadcrumb("Payment-Execute-Error", "Sdk Payment", "error", data = mapOf("error_type" to t.javaClass.simpleName, "error_msg" to (t.message ?: "")))
        unbusy()
        retrow(t)
      }
    }

    fun parseContactPhone(raw: String): ContactPhone? {
      val trimmed = raw.replace("\\s".toRegex(), "")
      if (trimmed.isEmpty()) return null
      val util = PhoneNumberUtil.getInstance()
      return try {
        val num = util.parse(trimmed, null)
        ContactPhone(
          countryCode = num.countryCode.toString(),
          nationalNumber = num.nationalNumber.toString(),
          fullNumber = util.format(num, PhoneNumberUtil.PhoneNumberFormat.E164),
          countryIso = util.getRegionCodeForCountryCode(num.countryCode)
        )
      } catch (_: NumberParseException) {
        null
      }
    }

    fun prepareChallengeConfig(params: ChallengeParameters) = threeDs.prepareChallengeConfig(params)

    suspend fun executeGooglePayPayment(
      paymentDataJson: String,
      bindingCreationIsNeeded: Boolean = false,
      bindingName: String? = null
    ) {
      try {
        busy()

        lib.metrica.breadcrumb("Payment-Execute-Initiated", "Sdk Payment", "action", data = mapOf("method" to TYPE_GPAY, "create_binding" to bindingCreationIsNeeded))

        val order = internal.order ?: throw IllegalStateException("Order data not available")
        val orderId = order.id

        val pm = OrderApi.Models.PaymentMethod(
          type = TYPE_GPAY,
          paymentData = Json.parseToJsonElement(paymentDataJson)
        )

        val contact = state.value.contact
        val email = contact?.email?.takeIf(String::isNotBlank)
        val phoneParsed = contact?.phone?.takeIf(String::isNotBlank)?.let(::parseContactPhone)
        val payer = if (email == null && phoneParsed == null) null
        else OrderApi.Models.Payer(
          contactEmail = email,
          contactPhone = phoneParsed?.let { OrderApi.Models.Phone(it.countryCode, it.nationalNumber, it.fullNumber) }
        )

        val req = OrderApi.Models.ExecutePaymentRequest(
          paymentMethod = pm,
          deviceData = createDeviceData(),
          bindingCreationIsNeeded = bindingCreationIsNeeded,
          bindingName = bindingName,
          payer = payer
        )

        val resp = RemoteExecutePayment(lib, orderId, req)
        threeDs.handleIfNeeded(resp, orderId)?.let { _effects.send(it) }
      } catch (t: Exception) {
        lib.metrica.breadcrumb("Payment-Execute-Error", "Sdk Payment", "error", data = mapOf("method" to TYPE_GPAY, "error_type" to t.javaClass.simpleName, "error_msg" to (t.message ?: "")))
        unbusy()
        retrow(t)
      }
    }

    private fun convertToYYMM(mmyy: String) = if (mmyy.length == 4) mmyy.takeLast(2) + mmyy.take(2) else mmyy

    private fun createDeviceData(): OrderApi.Models.DeviceData {
      val dev = lib.state.device
      return OrderApi.Models.DeviceData(
        threedsSdkData = OrderApi.Models.ThreedsSDKData(name = "3dssdk", "1.0.0", null),
        ip = dev.ip
      )
    }

    fun pan(pan: String) {
      val digits = pan.filter(Char::isDigit).take(PAN_MAX)
      val prev = state.value.fields.pan
      if (prev == digits) return

      updateFields {
        it.copy(
          pan = digits,
          panNetwork = if (digits.length < PAN_MIN) null else it.panNetwork,
          panBusy = digits.length in PAN_MIN..PAN_MAX,
          panError = null,
          cvvLength = if (digits.length < PAN_MIN) FALLBACK_CVV else it.cvvLength
        )
      }

      val targetLen = state.value.fields.panNetwork?.pan
      if (targetLen != null && digits.length == targetLen) {
        launch { _effects.send(Effect.FocusExp) }
      }
    }

    fun exp(exp: String) {
      val old = state.value.fields.exp
      val digits = exp.filter(Char::isDigit).take(4)
      if (old == digits) return

      updateFields { it.copy(exp = digits, expError = null) }

      when {
        digits.length == 4 -> launch { _effects.send(Effect.FocusCvv) }
        digits.isEmpty() && old.isNotEmpty() -> launch { _effects.send(Effect.FocusPan) }
      }
    }

    fun cvv(cvv: String) {
      val old = state.value.fields.cvv
      val max = state.value.fields.cvvLength
      val digits = cvv.filter(Char::isDigit).take(max)
      if (old == digits) return

      updateFields { it.copy(cvv = digits, cvvError = null) }

      if (digits.length == max) {
        val s = state.value
        launch {
          when {
            s.fields.cardHolderNameAvailable -> _effects.send(Effect.FocusCardholder)
            s.saved?.savingAvailable == true -> _effects.send(Effect.HideKeyboard)
            else -> _effects.send(Effect.HideKeyboard)
          }
        }
      } else if (digits.isEmpty() && old.isNotEmpty()) {
        launch { _effects.send(Effect.FocusExp) }
      }
    }

    fun cn(cn: String) {
      val clean = cn.take(200)
      if (state.value.fields.cn == clean) return
      updateFields { it.copy(cn = clean, cnError = null) }
    }

    fun toggleSave(save: Boolean) = update { it.copy(saving = save) }
      .also {
        lib.metrica.breadcrumb("SaveCard-Toggled", "Sdk UI", "action", data = mapOf("enabled" to save))
      }

    fun panFocusLost() {
      val digits = state.value.fields.pan.filter(Char::isDigit)
      val ext = state.value
      val net = ext.fields.panNetwork
      val cat = ext.fields.panCategory

      when {
        !luhn(digits) -> updateFields { it.copy(panError = Text.Resource(R.string.error_invalid_card_number)) }
        (net != null && net !in ext.networks) || (cat != null && cat !in internal.allowedCategories) ->
          updateFields { it.copy(panError = Text.Resource(R.string.error_payment_network_not_supported)) }
      }

      cvvFocusLost()
    }

    fun expFocusLost() {
      val exp = state.value.fields.exp
      if (exp.length == 4) {
        val m = exp.take(2).toIntOrNull()
        val y = exp.substring(2, 4).toIntOrNull()
        if (m == null || y == null || m !in 1..12) {
          updateFields { it.copy(expError = Text.Resource(R.string.error_invalid_exp_number)) }
        }
      }
    }

    fun cvvFocusLost() {
      val cvv = state.value.fields.cvv
      val req = state.value.fields.cvvLength
      if (cvv.isNotEmpty() && cvv.length < req) {
        updateFields { it.copy(cvvError = Text.Resource(R.string.error_invalid_cvv)) }
      }
    }

    fun cnFocusLost() {
      val cn = state.value.fields.cn
      if (cn.length > 200) updateFields { it.copy(cnError = Text.Resource(R.string.error_invalid_card_name)) }
    }

    fun ch(ch: String) {
      val value = ch.uppercase()
      if (state.value.fields.ch == value) return

      val st = when {
        value.isBlank() -> FieldState.EMPTY
        isChValid(value) -> FieldState.VALID
        else -> FieldState.INVALID_SILENT
      }

      updateFields {
        it.copy(
          ch = value,
          chState = st,
          chDirty = true,
          chError = if (st == FieldState.INVALID_SILENT) null else it.chError
        )
      }
    }

    private fun finalizeChNow(value: String) {
      val st = when {
        value.isBlank() -> FieldState.EMPTY
        isChValid(value) -> FieldState.VALID
        else -> FieldState.INVALID_VISIBLE
      }
      val err = if (st == FieldState.INVALID_VISIBLE) Text.Resource(R.string.error_cardholder_name_invalid_chars) else null
      updateFields { it.copy(chState = st, chError = err, chDirty = false) }
    }

    fun chFocusLost() {
      finalizeChNow(state.value.fields.ch)
    }

    fun unbusy() = update { it.copy(busy = false) }

    fun setPaymentScreenMode(mode: State.Mode) = update { it.copy(mode = mode) }
      .also {
        lib.metrica.breadcrumb("Mode-Switched", "Sdk UI", "action", data = mapOf("mode" to mode.name))
      }

    fun selectSavedCard(id: String) = update {
      it.copy(saved = it.saved?.copy(selectedCardId = id, cvvInput = "", cvvError = null))
    }.also {
      lib.metrica.breadcrumb("SavedCard-Selected", "Sdk UI", "action")
    }

    fun savedCardCvv(cvv: String) {
      val max = state.value.saved?.cards?.find { it.id == state.value.saved?.selectedCardId }?.cvvLength ?: FALLBACK_CVV
      if (cvv.length <= max && cvv.all(Char::isDigit)) {
        update { it.copy(saved = it.saved?.copy(cvvInput = cvv, cvvError = null)) }
      }
    }

    fun savedCardCvvFocusLost() {
      val s = state.value.saved ?: return
      val card = s.cards.find { it.id == s.selectedCardId } ?: return
      if (s.cvvInput.length != card.cvvLength) {
        update { it.copy(saved = it.saved?.copy(cvvError = Text.Resource(R.string.error_invalid_cvv))) }
      }
    }

    fun deleteCard(id: String) {
      launch {
        val card = state.value.saved?.cards?.find { it.id == id } ?: return@launch
        val name = card.cardName + " *" + card.maskedPan.takeLast(4)
        with(key()) {
          _effects.send(Effect.ConfirmDeleteCard(cardId = id, cardName = name, requestKey = this))
          lib.navigation.await<NavigationEvents.Event.ConfirmCardRemove>(this)
          confirmDeleteCard(id)
        }

        lib.metrica.breadcrumb("SavedCard-Delete-Requested", "Sdk UI", "action")
      }
    }

    fun confirmDeleteCard(id: String) {
      update { it.copy(saved = it.saved?.copy(isLoadingCards = true)) }

      launch {
        lib.metrica.breadcrumb("SavedCard-Delete-Initiated", "Sdk Payment", "action")

        runCatching { RemoteDeleteBinding(lib, id) }
          .onFailure { if (it !is IntegrationException.NoResponseError) e("Delete binding failed: ${it.message}", it) else retrow(it) }

        val cards = loadSavedCards(internal.order!!, internal.allowedSchemes, internal.allowedCategories) ?: emptyList()
        val mode = if (cards.isEmpty()) State.Mode.NewCard else state.value.mode

        handle.setMode(mode)
        update { it.copy(mode = mode, saved = it.saved?.copy(cards = cards, isLoadingCards = false)) }
        lib.metrica.breadcrumb("SavedCard-Delete-Completed", "Sdk Payment", "state", data = mapOf("remaining_cards" to cards.size))
      }
    }

    fun editCard(id: String) {
      launch {
        val name = state.value.saved?.cards?.find { it.id == id }?.cardName ?: return@launch
        with(key()) {
          _effects.send(Effect.EditCard(cardId = id, cardName = name, requestKey = this))
          val result = lib.navigation.await<NavigationEvents.Event.ConfirmCardEdit>(this)
          confirmEditCard(id, result.cardName)
        }

        lib.metrica.breadcrumb("SavedCard-Edit-Requested", "Sdk UI", "action")
      }
    }

    fun confirmEditCard(id: String, name: String) {
      update { it.copy(saved = it.saved?.copy(isLoadingCards = true)) }

      launch {
        lib.metrica.breadcrumb("SavedCard-Edit-Initiated", "Sdk Payment", "action")

        runCatching { RemoteEditBinding(lib, id, name) }
          .onFailure { if (it !is IntegrationException.NoResponseError) e("Edit binding failed: ${it.message}", it) else retrow(it) }

        val cards = loadSavedCards(internal.order!!, internal.allowedSchemes, internal.allowedCategories) ?: emptyList()
        val mode = if (cards.isEmpty()) State.Mode.NewCard else state.value.mode

        handle.setMode(mode)
        update { it.copy(mode = mode, saved = it.saved?.copy(cards = cards, isLoadingCards = false)) }
        lib.metrica.breadcrumb("SavedCard-Edit-Finished", "Sdk Payment", "state")
      }
    }

    val isEligibleToPay: Boolean
      get() = with(state.value) {
        if (mode == State.Mode.SavedCard) {
          val s = saved ?: return@with false
          val card = s.cards.find { it.id == s.selectedCardId }
          !busy &&
            !s.isLoadingCards &&
            card != null &&
            card.isAvailable &&
            s.cvvInput.isNotBlank() &&
            s.cvvInput.length == card.cvvLength &&
            s.cvvError == null
        } else {
          val f = fields
          !busy &&
            !f.panBusy &&
            contact?.busy != true &&
            f.pan.isNotBlank() &&
            f.exp.isNotBlank() &&
            f.cvv.isNotBlank() &&
            f.cvv.length == f.panNetwork?.cvc &&
            f.panError == null &&
            f.expError == null &&
            f.cvvError == null &&
            (!f.cardHolderNameAvailable || f.chState == FieldState.VALID) &&
            (saved?.available == false || f.cnError == null)
        }
      }
  }

  data class ContactPhone(
    val countryCode: String,
    val nationalNumber: String,
    val fullNumber: String,
    val countryIso: String
  )

  private companion object {
    const val KEY_MODE = "mode"

    const val TYPE_CARD = "PAYMENT_CARD"
    const val TYPE_BINDING = "PAYMENT_CARD_BINDING"
    const val TYPE_GPAY = "GOOGLE_PAY"

    const val S_CREATED = "CREATED"
    const val S_PAID = "PAID"
    const val S_DECLINED = "DECLINED"
    const val S_PARTIALLY_REFUNDED = "PARTIALLY_REFUNDED"
    const val S_REFUNDED = "REFUNDED"
    const val S_CANCELLED = "CANCELLED"
    const val S_EXPIRED = "EXPIRED"
    const val S_RECURRENCE_ACTIVE = "RECURRENCE_ACTIVE"
    const val S_RECURRENCE_CLOSED = "RECURRENCE_CLOSED"

    const val PAN_MIN = 6
    const val PAN_MAX = 19
    const val PAN_DEBOUNCE_MS = 300L
    const val FALLBACK_CVV = 4
  }
}