@file:Suppress("OPT_IN_USAGE")

package com.guavapay.paymentsdk.presentation.screens.mainpage

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.gateway.banking.GatewayException
import com.guavapay.paymentsdk.gateway.banking.GatewayException.ClientException.NoAvailableCardProductCategoriesException
import com.guavapay.paymentsdk.gateway.banking.GatewayException.ClientException.NoAvailableCardSchemesException
import com.guavapay.paymentsdk.gateway.banking.GatewayException.UnknownException
import com.guavapay.paymentsdk.gateway.banking.PaymentCardCategory
import com.guavapay.paymentsdk.gateway.banking.PaymentCardScheme
import com.guavapay.paymentsdk.gateway.banking.PaymentKind
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod.Card
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod.GooglePay
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod.SavedCard
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.gateway.banking.PaymentResult.Companion.toResult
import com.guavapay.paymentsdk.gateway.vendors.googlepay.GPayResult
import com.guavapay.paymentsdk.integrations.IntegrationException.ClientError
import com.guavapay.paymentsdk.integrations.remote.RemoteCardRangeData
import com.guavapay.paymentsdk.integrations.remote.RemoteContinuePayment
import com.guavapay.paymentsdk.integrations.remote.RemoteExecutePayment
import com.guavapay.paymentsdk.integrations.remote.RemoteGooglePayContext
import com.guavapay.paymentsdk.integrations.remote.RemoteOrderSubscription
import com.guavapay.paymentsdk.logging.i
import com.guavapay.paymentsdk.logging.w
import com.guavapay.paymentsdk.network.services.OrderApi
import com.guavapay.paymentsdk.network.services.OrderApi.Models.GetOrderResponse
import com.guavapay.paymentsdk.platform.algorithm.luhn
import com.guavapay.paymentsdk.platform.arrays.intersectByName
import com.guavapay.paymentsdk.presentation.platform.Text
import com.guavapay.paymentsdk.presentation.platform.currencify
import com.guavapay.myguava.business.myguava3ds2.init.ui.GUiCustomization
import com.guavapay.myguava.business.myguava3ds2.observability.ErrorReporter
import com.guavapay.myguava.business.myguava3ds2.security.MessageTransformerFactory
import com.guavapay.myguava.business.myguava3ds2.security.MyGuavaEphemeralKeyPairGenerator
import com.guavapay.myguava.business.myguava3ds2.service.GuavaThreeDs2ServiceImpl
import com.guavapay.myguava.business.myguava3ds2.transaction.ChallengeParameters
import com.guavapay.myguava.business.myguava3ds2.transaction.ChallengeRequestExecutor
import com.guavapay.myguava.business.myguava3ds2.transaction.InitChallengeRepositoryFactory
import com.guavapay.myguava.business.myguava3ds2.transaction.SdkTransactionId
import com.guavapay.myguava.business.myguava3ds2.transaction.Transaction
import com.guavapay.myguava.business.myguava3ds2.transactions.ChallengeRequestData
import com.guavapay.myguava.business.myguava3ds2.transactions.MessageExtension
import com.guavapay.myguava.business.myguava3ds2.views.Brand
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

internal class MainVM(private val lib: LibraryUnit) : ViewModel() {
  private val payload = lib.state.payload()

  private val _state = MutableStateFlow(State()) // Todo: Use only internal state for ui.
  val state: StateFlow<State> = _state.asStateFlow()

  private val _effects = Channel<Effect>(BUFFERED, DROP_OLDEST)
  val effects: Flow<Effect> = _effects.receiveAsFlow()

  private val instruments = Instruments() ; inner class Instruments {
    val card = payload.methods.filterIsInstance<Card>().firstOrNull()
    val saved = payload.methods.filterIsInstance<SavedCard>().firstOrNull()
    val gpay = payload.methods.filterIsInstance<GooglePay>().firstOrNull()
  }

  lateinit var uiCustomization: GUiCustomization

  init {
    resolvers()
  }

  private val threeds = GuavaThreeDs2ServiceImpl(context = lib.context, enableLogging = true, workContext = lib.coroutine.dispatchers.common)

  private fun busy() = _state.update { state -> state.copy(external = state.external.copy(busy = true)) }
  private fun unbusy() = _state.update { state -> state.copy(external = state.external.copy(busy = false)) }

  private fun busyPan() = _state.update { state -> state.copy(external = state.external.copy(fields = state.external.fields.copy(panBusy = true))) }
  private fun unbusyPan() = _state.update { state -> state.copy(external = state.external.copy(fields = state.external.fields.copy(panBusy = false))) }

  private suspend fun fatal(throwable: GatewayException? = null) = _effects.send(Effect.AbortDueError(throwable))
  private fun fieldError() = _state.update { state ->
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

  private fun resolvers() {
    RemoteOrderSubscription(lib)
      .onStart { busy() }
      .distinctUntilChanged()
      .onEach { resolveOrder(it); unbusy() }
      .catch { unbusy(); fatal(UnknownException(it)) }
      .launchIn(viewModelScope)

    _state
      .map { it.external.fields.pan.filter(Char::isDigit) }
      .distinctUntilChanged()
      .filter { digits -> digits.length in 6..19 }
      .debounce(300)
      .onEach { busyPan() }
      .onEach(::resolvePan)
      .onEach { unbusyPan() }
      .catch { if (it is ClientError) fieldError() else fatal(UnknownException(it)) }
      .launchIn(viewModelScope)
  }

  sealed class OrderStatusException : Exception() {
    class OrderStatusUnprocessable(val status: String) : OrderStatusException()
    class OrderExpired(val status: String) : OrderStatusException()
    class OrderCancelled(val status: String) : OrderStatusException()
  }

  private suspend fun resolveOrder(order: GetOrderResponse) {
    i(order.toString())

    when (order.order.status) {
      "CREATED" -> {i("Order status is CREATED, processing...") }
      "PAID" -> {
        unbusy()
        i("Payment completed successfully with status PAID")
        val result = PaymentResult.Success(payment = order.payment?.toResult(), order = order.order.toResult())
        _effects.send(Effect.FinishPayment(result))
        return
      }
      "DECLINED" -> {
        unbusy()
        i("Payment declined with status DECLINED")
        val result = PaymentResult.Unsuccess(payment = order.payment?.toResult(), order = order.order.toResult())
        _effects.send(Effect.FinishPayment(result))
        return
      }
      "PARTIALLY_REFUNDED" -> {
        unbusy()
        i("Payment partially refunded with status PARTIALLY_REFUNDED")
        val result = PaymentResult.Success(payment = order.payment?.toResult(), order = order.order.toResult())
        _effects.send(Effect.FinishPayment(result))
        return
      }
      "REFUNDED" -> {
        unbusy()
        i("Payment refunded with status REFUNDED")
        val result = PaymentResult.Success(payment = order.payment?.toResult(), order = order.order.toResult())
        _effects.send(Effect.FinishPayment(result))
        return
      }
      "CANCELLED" -> {
        unbusy()
        i("Payment cancelled with status CANCELLED")
        val result = PaymentResult.Unsuccess(payment = order.payment?.toResult(), order = order.order.toResult())
        _effects.send(Effect.FinishPayment(result))
        return
      }
      "EXPIRED" -> {
        unbusy()
        i("Payment expired with status EXPIRED")
        val result = PaymentResult.Unsuccess(payment = order.payment?.toResult(), order = order.order.toResult())
        _effects.send(Effect.FinishPayment(result))
        return
      }
      "RECURRENCE_ACTIVE" -> {
        unbusy()
        i("Payment recurrence active with status RECURRENCE_ACTIVE")
        val result = PaymentResult.Success(payment = order.payment?.toResult(), order = order.order.toResult())
        _effects.send(Effect.FinishPayment(result))
        return
      }
      "RECURRENCE_CLOSED" -> {
        unbusy()
        i("Payment recurrence closed with status RECURRENCE_CLOSED")
        val result = PaymentResult.Success(payment = order.payment?.toResult(), order = order.order.toResult())
        _effects.send(Effect.FinishPayment(result))
        return
      }
      else -> {
        unbusy()
        w("Unexpected order status: ${order.order.status}")
        val result = PaymentResult.Error(OrderStatusException.OrderStatusUnprocessable(order.order.status))
        _effects.send(Effect.FinishPayment(result))
        return
      }
    }

    try {
      val formattedAmount = currencify(order.order.totalAmount.baseUnits, order.order.totalAmount.currency, payload.locale())

      val sdkSupportedMethods = payload.methods.mapNotNull { method ->
        when (method) {
          is Card -> "PAYMENT_CARD"
          is GooglePay -> "GOOGLE_PAY"
          is SavedCard -> "PAYMENT_CARD_BINDING"
          else -> null
        }
      }.toSet()

      val availablePaymentMethods = order.order.availablePaymentMethods.intersect(sdkSupportedMethods)

      val sdkSupportedNetworks = payload.schemes.map { it.name }.toSet()

      val availableCardSchemes = order.order.availableCardSchemes
        .intersectByName(sdkSupportedNetworks)
        .toList()

      if (availableCardSchemes.isEmpty()) {
        _effects.send(Effect.AbortDueError(NoAvailableCardSchemesException("No available card schemes")))
        return
      }

      val sdkSupportedCardTypes = payload.categories.map { it.name }.toSet()

      val availableCardProductCategories = order.order.availableCardProductCategories.intersectByName(sdkSupportedCardTypes)

      if (availableCardProductCategories.isEmpty()) {
        _effects.send(Effect.AbortDueError(NoAvailableCardProductCategoriesException("No available card product categories")))
        return
      }

      val googlePayContext = if ("GOOGLE_PAY" in availablePaymentMethods && instruments.gpay != null) {
        try {
          val ctx = RemoteGooglePayContext(lib).context
          val availableSchemes = availableCardSchemes.map(PaymentCardScheme::name).toSet()
          ctx.copy(allowedCardSchemes = ctx.allowedCardSchemes.intersect(availableSchemes).toList())
        } catch (e: Exception) {
          w("Failed to load GooglePay context: $e")
          null
        }
      } else {
        null
      }

      val contactState = order.order.payer?.let { payer ->
        State.ExternalState.ContactState(
          email = payer.maskedContactEmail ?: payer.contactEmail ?: "",
          phone = payer.maskedContactPhone?.formatted ?: payer.contactPhone?.fullNumber ?: ""
        )
      }

      _state.update { state ->
        state.copy(
          internal = state.internal.copy(
            orderData = order.order,
            googlePayContext = googlePayContext,
            availablePaymentMethods = availablePaymentMethods,
            availableCardSchemes = availableCardSchemes,
            availableCardProductCategories = availableCardProductCategories,
            availablePaymentCurrencies = order.order.availablePaymentCurrencies
          ),
          external = state.external.copy(
            paytext = Text.Plain(formattedAmount),
            paykind = payload.kind,
            networks = availableCardSchemes,
            contact = contactState,
            saved = State.ExternalState.SavedState(
              available = "PAYMENT_CARD_BINDING" in availablePaymentMethods &&
                instruments.saved != null
            ),
            gpay = State.ExternalState.GPayState(
              available = "GOOGLE_PAY" in availablePaymentMethods &&
                instruments.gpay != null &&
                googlePayContext != null &&
                order.order.availablePaymentCurrencies.isNotEmpty()
            )
          )
        )
      }

    } catch (e: Exception) {
      w("Error in resolveOrder: $e")
      fatal(UnknownException(e))
    }
  }

  private suspend fun resolvePan(pan: String) {
    val data = RemoteCardRangeData(lib, pan)
    if (data.cardScheme != null || data.product != null) {
      _state.update { state ->
        state.copy(
          external = state.external.copy(
            fields = state.external.fields.copy(
              panBusy = false,
              panNetwork = data.cardScheme,
              cvvLength = data.cardScheme!!.cvc,
              panCategory = data.product?.category
            )
          )
        )
      }
    }
  }

  val handles = Handles() ; inner class Handles {
    fun pay() {
      if (!isEligibleToPay) return

      val state = state.value.external

      val contact = state.contact
      /*if (contact == null || contact.email.isBlank() || contact.phone.isBlank()) {
        viewModelScope.launch {
          _effects.send(Effect.RequiredContacts)
        }
        return
      }*/

      viewModelScope.launch {
        try {
          executeCardPayment(
            pan = state.fields.pan,
            cvv = state.fields.cvv,
            expiryDate = convertToYYMM(state.fields.exp),
            cardholderName = if (state.saving && state.fields.cn.isNotBlank()) state.fields.cn else null,
            bindingCreationIsNeeded = state.saving,
            bindingName = if (state.saving) state.fields.cn.takeIf { it.isNotBlank() } else null
          )
          i("Card payment execute initiated, waiting for SSE status")
        } catch (e: Exception) {
          _effects.send(Effect.AbortDueError(UnknownException(e)))
        }
      }
    }

    fun gpay(result: GPayResult) {
      viewModelScope.launch {
        println(result)
        when (result) {
          is GPayResult.Success -> {
            val external = state.value.external
            try {
              executeGooglePayPayment(
                paymentDataJson = result.data,
                bindingCreationIsNeeded = external.saving,
                bindingName = if (external.saving) external.fields.cn.takeIf { it.isNotBlank() } else null
              )
              i("Google Pay payment execute initiated, waiting for SSE status")
            } catch (e: Exception) {
              _effects.send(Effect.AbortDueError(UnknownException(e)))
            }
          }
          is GPayResult.Failed -> _effects.send(Effect.PaymentError(Text.Plain(result.throwable?.message ?: "Google Pay error")))
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
        application,
        isLiveMode,
        sdkTransactionId,
        uiCustomization,
        rootCerts = emptyList(),
        enableLogging = false,
        coroutineContext
      ).create()

    fun prepareChallengeParameters(
      threeDSRequestorAppURL: String? = null,
      encodedParams: String
    ): ChallengeParameters {
      return ChallengeParameters.prepareFromPaymentApiString(
        threeDSRequestorAppURL = threeDSRequestorAppURL,
        text = encodedParams
      )
    }

    suspend fun executeCardPayment(
      pan: String,
      cvv: String,
      expiryDate: String,
      cardholderName: String? = null,
      bindingCreationIsNeeded: Boolean = false,
      bindingName: String? = null
    ) {
      try {
        busy()

        val orderData = state.value.internal.orderData ?: throw UnknownException(IllegalStateException("Order data not available"))
        val orderId = orderData.id

        val paymentMethod = OrderApi.Models.PaymentMethod(
          type = "PAYMENT_CARD",
          pan = pan,
          cvv2 = cvv,
          expiryDate = expiryDate,
          cardholderName = cardholderName
        )

        val executeRequest = OrderApi.Models.ExecutePaymentRequest(
          paymentMethod = paymentMethod,
          deviceData = createDeviceData(null),
          bindingCreationIsNeeded = bindingCreationIsNeeded,
          bindingName = bindingName,
          payer = OrderApi.Models.Payer(
            contactEmail = "personal@mairwunnx.com",
            contactPhone = OrderApi.Models.Phone(
              countryCode = "1",
              nationalNumber = "9077756511".takeLast(10),
              fullNumber = "+19077756511"
            )
          )
        )

        val response = RemoteExecutePayment(lib, orderId, executeRequest)
        handle3dsFlowIfRequired(response, orderId)

        i("Card payment execute request completed, staying busy until SSE final status")
      } catch (e: Exception) {
        unbusy()
        w("Card execute payment failed: $e")
        throw e
      }
    }

    private var transaction: Transaction? = null

    private suspend fun handle3dsFlowIfRequired(
      response: OrderApi.Models.ExecutePaymentResponse,
      orderId: String
    ) {
      if (response.requirements?.threedsSdkCreateTransaction != null) {
        val repository = getChallengeRepositoryFactory(coroutineContext = currentCoroutineContext())

        val directoryServerId = response.requirements.threedsSdkCreateTransaction.directoryServerID!!
        val messageVersion = response.requirements.threedsSdkCreateTransaction.messageVersion!!
        val directoryServerName = when (directoryServerId) {
          "A000000003" -> "visa"
          "A000000004" -> "mastercard"
          "A000000152" -> "diners"
          "A000000324" -> "discover"
          "A000000025" -> "amex"
          else -> "visa" // Default to Visa if unknown
        }
        val isLiveMode = true

        val sdkTransactionId = SdkTransactionId.create()
        val dsPublicKey = threeds.getPublicKey(directoryServerId)

        transaction = threeds.createTransaction(
          sdkTransactionId = sdkTransactionId,
          directoryServerID = directoryServerId,
          messageVersion = messageVersion,
          isLiveMode = isLiveMode,
          directoryServerName = directoryServerName,
          rootCerts = emptyList(),
          dsPublicKey = dsPublicKey,
          keyId = null,
          uiCustomization = uiCustomization
        )
        val authRequestParams = transaction!!.createAuthenticationRequestParameters()
        val paymentcontinue = OrderApi.Models.ContinuePaymentRequest(
          threedsSdkData = OrderApi.Models.ThreedsSDKData(
            name = "3dssdk",
            version = "1.0.0",
            packedAuthenticationData = authRequestParams.toPaymentApiString()
          )
        )

        val response2 = RemoteContinuePayment(lib, orderId, paymentcontinue)
        if (response2.requirements?.threedsChallenge != null) {
          val requirements = response2.requirements

          if (requirements.threedsChallenge != null) {
            val encodedParams = requirements.threedsChallenge.packedSdkChallengeParameters
            if (encodedParams != null) {
              val challengeParameters = prepareChallengeParameters(
                threeDSRequestorAppURL = "https://google.com/",
                encodedParams = encodedParams
              )
              _effects.send(Effect.ChallengeRequired(challengeParameters, transaction!!))
            } else if (requirements.threedsChallenge.url?.isNotEmpty() == true) {
//                _state.value = PaymentState.RedirectRequired(requirements.threedsChallenge.url)
            } else {
//                _state.value = PaymentState.Error("Challenge required but no parameters received")
            }
          } else if (requirements?.payerAuthorization != null &&
            !requirements.payerAuthorization.authorizationUrl.isNullOrEmpty()
          ) {
//              _state.value = PaymentState.RedirectRequired(requirements.payerAuthorization.authorizationUrl)
          } else if (requirements?.payPalOrderApprove != null &&
            !requirements.payPalOrderApprove.actionUrl.isNullOrEmpty()
          ) {
//              _state.value = PaymentState.RedirectRequired(requirements.payPalOrderApprove.actionUrl)
          } else {
//              _state.value = PaymentState.PaymentCompleted(response)
          }
        }
      }
    }

    fun prepareChallengeConfig(challengeParameters: ChallengeParameters): ChallengeRequestExecutor.Config? {
      return createChallengeArgs(transaction = transaction ?: return null, challengeParameters = challengeParameters).getOrNull()
    }

    fun createChallengeArgs(
      transaction: Transaction,
      challengeParameters: ChallengeParameters,
      timeoutMins: Int = 5
    ): Result<ChallengeRequestExecutor.Config> {
      return try {
        val messageTransformer = MessageTransformerFactory(false).create()
        val fakeErrorReporter = ErrorReporter { }

        val keyPair = MyGuavaEphemeralKeyPairGenerator(fakeErrorReporter).generate()
        val acsUrl = ACS_URL
        val sdkReferenceId = challengeParameters.acsRefNumber.orEmpty()

        val threeDSRequestorAppURL = THREE_DS_REQUESTOR_APP_URL
        val creqData = ChallengeRequestData(
          acsTransId = challengeParameters.acsTransactionId ?: "",
          threeDsServerTransId = challengeParameters.threeDsServerTransactionId ?: "",
          sdkTransId = transaction.sdkTransactionId,
          messageVersion = "2.2.0",
          messageExtensions = getMessageExtensions(challengeParameters, transaction.getBrand()),
          threeDSRequestorAppURL = threeDSRequestorAppURL,
        )

        val creqExecutorConfig = ChallengeRequestExecutor.Config(
          messageTransformer = messageTransformer,
          sdkReferenceId = sdkReferenceId,
          acsUrl = acsUrl,
          creqData = creqData,
          keys = ChallengeRequestExecutor.Config.Keys(
            keyPair.private.encoded,
            keyPair.public.encoded
          )
        )

        Result.success(creqExecutorConfig)
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

    private fun getMessageExtensions(challengeParameters: ChallengeParameters, brand: Brand): List<MessageExtension>? {
      return listOfNotNull(
        MessageExtension.createBridgingExtension(
          oobAppURLInd = "01",
          oobContinue = null
        )
      ).takeIf { challengeParameters.threeDSRequestorAppURL.orEmpty().isNotEmpty() && brand == Brand.Mastercard }
    }

    suspend fun executeGooglePayPayment(
      paymentDataJson: String,
      bindingCreationIsNeeded: Boolean = false,
      bindingName: String? = null
    ) {
      try {
        busy()

        val orderData = state.value.internal.orderData ?: throw IllegalStateException("Order data not available")
        val orderId = orderData.id

        val paymentDataElement = Json.parseToJsonElement(paymentDataJson)

        val paymentMethod = OrderApi.Models.PaymentMethod(
          type = "GOOGLE_PAY",
          paymentData = paymentDataElement
        )

        val contact = state.value.external.contact
        val payer = if (contact != null && contact.email.isNotBlank() && contact.phone.isNotBlank()) {
          OrderApi.Models.Payer(
            contactEmail = contact.email,
            contactPhone = parsePhoneNumber(contact.phone)
          )
        } else null

        val executeRequest = OrderApi.Models.ExecutePaymentRequest(
          paymentMethod = paymentMethod,
          deviceData = createDeviceData(null),
          bindingCreationIsNeeded = bindingCreationIsNeeded,
          bindingName = bindingName,
          payer = payer
        )

        i("Executing Google Pay payment after pre-create...")
        val response = RemoteExecutePayment(lib, orderId, executeRequest)

        handle3dsFlowIfRequired(response, orderId)

        i("Google Pay payment execute request completed, staying busy until SSE final status")
      } catch (e: Exception) {
        unbusy()
        w("Google Pay execute payment failed: $e")
        throw e
      }
    }

    // TODO: REFACTOR FOR FIRST VERSION ONLY
    private fun parsePhoneNumber(phoneNumber: String): OrderApi.Models.Phone? {
      val digits = phoneNumber.filter { it.isDigit() }
      return if (digits.length >= 10) {
        OrderApi.Models.Phone(
          countryCode = "1",
          nationalNumber = digits.takeLast(10),
          fullNumber = phoneNumber
        )
      } else null
    }

    // TODO: Use kotlinx.serialization adapter module.
    private fun convertToYYMM(mmyy: String): String {
      return if (mmyy.length == 4) {
        val mm = mmyy.substring(0, 2)
        val yy = mmyy.substring(2, 4)
        "$yy$mm"
      } else {
        mmyy
      }
    }

    private fun createDeviceData(packetAuthenticationData: String?): OrderApi.Models.DeviceData {
      val deviceData = lib.state.device

      return OrderApi.Models.DeviceData(
        threedsSdkData = OrderApi.Models.ThreedsSDKData(name = "3dssdk", "1.0.0", packetAuthenticationData),
        ip = deviceData.ip
      )
    }

    fun pan(pan: String) {
      val digitsOnly = pan.filter(Char::isDigit).take(19)

      val currentPan = _state.value.external.fields.pan
      if (currentPan == digitsOnly) {
        return
      }

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

      if (digitsOnly.length == _state.value.external.fields.panNetwork?.pan) {
        viewModelScope.launch { _effects.send(Effect.FocusExp) }
      }
    }

    fun exp(exp: String) {
      val oldExp = _state.value.external.fields.exp
      val digitsOnly = exp.filter(Char::isDigit).take(4)

      if (oldExp == digitsOnly) {
        return
      }

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

      if (digitsOnly.length == 4) {
        viewModelScope.launch { _effects.send(Effect.FocusCvv) }
      } else if (digitsOnly.isEmpty() && oldExp.isNotEmpty()) {
        viewModelScope.launch { _effects.send(Effect.FocusPan) }
      }
    }

    fun cvv(cvv: String) {
      val oldCvv = _state.value.external.fields.cvv
      val maxLength = _state.value.external.fields.cvvLength
      val digitsOnly = cvv.filter(Char::isDigit).take(maxLength)

      if (oldCvv == digitsOnly) {
        return
      }

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

      if (digitsOnly.length == maxLength) {
        viewModelScope.launch { _effects.send(Effect.HideKeyboard) }
      } else if (digitsOnly.isEmpty() && oldCvv.isNotEmpty()) {
        viewModelScope.launch { _effects.send(Effect.FocusExp) }
      }
    }

    fun cn(cn: String) {
      val currentCn = _state.value.external.fields.cn
      val cleanCn = cn.take(32).trim()

      if (currentCn == cleanCn) {
        return
      }

      _state.update { state ->
        state.copy(
          external = state.external.copy(
            fields = state.external.fields.copy(
              cn = cleanCn,
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
      val currentState = _state.value.external
      val internalState = state.value.internal
      val currentNetwork = currentState.fields.panNetwork
      val currentCategory = currentState.fields.panCategory

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
      } else if ((currentNetwork != null && !currentState.networks.contains(currentNetwork)) ||
        (currentCategory != null && !internalState.availableCardProductCategories.contains(currentCategory))
      ) {
        _state.update { state ->
          state.copy(
            external = state.external.copy(
              fields = state.external.fields.copy(
                panError = Text.Resource(R.string.error_payment_network_not_supported)
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
      if (cn.length > 200) {
        _state.update { state ->
          state.copy(external = state.external.copy(fields = state.external.fields.copy(cnError = Text.Resource(R.string.error_invalid_card_name))))
        }
      }
    }

    fun unbusy() {
      _state.update { state ->
        state.copy(external = state.external.copy(busy = false))
      }
    }

    val isEligibleToPay: Boolean
      get() = with(state.value.external) {
        val fields = fields
        !busy &&
          !fields.panBusy &&
          contact?.busy != true &&
          fields.pan.isNotBlank() &&
          fields.exp.isNotBlank() &&
          fields.cvv.isNotBlank() &&
          fields.cvv.length == fields.panNetwork?.cvc &&
          fields.panError == null &&
          fields.expError == null &&
          fields.cvvError == null &&
          (saved?.available == false || fields.cnError == null)
      }
  }

  companion object {
    const val THREE_DS_REQUESTOR_APP_URL = "myguavaplayground://app/success" // todo: after first version, use real url
    const val ACS_URL = "https://bank.com"
  }

  data class State(val internal: InternalState = InternalState(), val external: ExternalState = ExternalState()) {
    data class InternalState(
      val orderData: OrderApi.Models.Order? = null,
      val googlePayContext: OrderApi.Models.GooglePayContext? = null,
      val availablePaymentMethods: Set<String> = emptySet(),
      val availableCardSchemes: List<PaymentCardScheme> = emptyList(),
      val availableCardProductCategories: Set<PaymentCardCategory> = emptySet(),
      val availablePaymentCurrencies: List<String> = emptyList(),
      val preCreateResult: OrderApi.Models.CreatePaymentResponse? = null
    )

    data class ExternalState(
      val fields: FieldsState = FieldsState(),
      val contact: ContactState? = null,
      val busy: Boolean = false,
      val saving: Boolean = false,
      val pay: Boolean = false,
      val paytext: Text? = null,
      val paykind: PaymentKind? = null,
      val networks: List<PaymentCardScheme> = emptyList(),
      val gpay: GPayState? = null,
      val saved: SavedState? = null,
    ) {
      data class FieldsState(
        val pan: String = "",
        val exp: String = "",
        val cvv: String = "",
        val cn: String = "",

        val panError: Text? = null,
        val expError: Text? = null,
        val cvvError: Text? = null,
        val cnError: Text? = null,

        val panNetwork: PaymentCardScheme? = null,
        val panCategory: PaymentCardCategory? = null,
        val panBusy: Boolean = false,
        val cvvLength: Int = 4,
      )

      data class ContactState(
        val email: String = "",
        val phone: String = "",
        val busy: Boolean = false,
      )

      data class GPayState(
        val available: Boolean = false
      )

      data class SavedState(
        val available: Boolean = false,
      )
    }
  }

  sealed interface Effect {
    data class PaymentError(val message: Text) : Effect
    data object RequiredContacts : Effect
    data class AbortDueError(val throwable: GatewayException? = null) : Effect
    data object AbortDueConditions : Effect
    data class FinishPayment(val result: PaymentResult) : Effect
    data object FocusPan : Effect
    data object FocusExp : Effect
    data object FocusCvv : Effect
    data object HideKeyboard : Effect
    data class ChallengeRequired(val challengeParameters: ChallengeParameters, val transaction: Transaction) : Effect
  }
}