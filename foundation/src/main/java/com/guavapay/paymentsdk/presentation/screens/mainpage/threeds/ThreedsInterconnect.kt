package com.guavapay.paymentsdk.presentation.screens.mainpage.threeds

import com.guavapay.myguava.business.myguava3ds2.init.ui.GUiCustomization
import com.guavapay.myguava.business.myguava3ds2.observability.ErrorReporter
import com.guavapay.myguava.business.myguava3ds2.security.MessageTransformerFactory
import com.guavapay.myguava.business.myguava3ds2.security.MyGuavaEphemeralKeyPairGenerator
import com.guavapay.myguava.business.myguava3ds2.service.GuavaThreeDs2ServiceImpl
import com.guavapay.myguava.business.myguava3ds2.transaction.ChallengeParameters
import com.guavapay.myguava.business.myguava3ds2.transaction.ChallengeRequestExecutor
import com.guavapay.myguava.business.myguava3ds2.transaction.SdkTransactionId
import com.guavapay.myguava.business.myguava3ds2.transaction.Transaction
import com.guavapay.myguava.business.myguava3ds2.transactions.ChallengeRequestData
import com.guavapay.myguava.business.myguava3ds2.transactions.MessageExtension.Companion.createBridgingExtension
import com.guavapay.myguava.business.myguava3ds2.views.Brand
import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.integrations.remote.RemoteContinuePayment
import com.guavapay.paymentsdk.logging.e
import com.guavapay.paymentsdk.network.services.OrderApi
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM

internal class ThreedsInterconnect(private val lib: LibraryUnit, private val ui: () -> GUiCustomization) {
  private val service = GuavaThreeDs2ServiceImpl(context = lib.context, enableLogging = true, workContext = lib.coroutine.dispatchers.common)

  private var tx: Transaction? = null

  suspend fun handleIfNeeded(response: OrderApi.Models.ExecutePaymentResponse, orderId: String): MainVM.Effect.Require3ds? {
    val create = response.requirements?.threedsSdkCreateTransaction ?: return null
    lib.metrica.breadcrumb("3ds-Init", "Sdk 3ds", "state")

    val dsId = create.directoryServerID!!
    val version = create.messageVersion!!
    val dsName = when (dsId) {
      "A000000003" -> "visa"
      "A000000004" -> "mastercard"
      "A000000152" -> "diners"
      "A000000324" -> "discover"
      "A000000025" -> "amex"
      else -> "visa"
    }

    val dsKey = service.getPublicKey(dsId)
    tx = service.createTransaction(
      sdkTransactionId = SdkTransactionId.create(),
      directoryServerID = dsId,
      messageVersion = version,
      isLiveMode = true,
      directoryServerName = dsName,
      rootCerts = emptyList(),
      dsPublicKey = dsKey,
      keyId = null,
      uiCustomization = ui()
    )

    lib.metrica.breadcrumb("3ds-Tx-Created", "Sdk 3ds", "state", data = mapOf("ds_id" to dsId, "version" to version))

    val auth = tx!!.createAuthenticationRequestParameters()
    val cont = OrderApi.Models.ContinuePaymentRequest(threedsSdkData = OrderApi.Models.ThreedsSDKData(name = "3dssdk", version = "0.5.0", packedAuthenticationData = auth.toPaymentApiString()))

    val contResp = RemoteContinuePayment(lib, orderId, cont)
    val chall = contResp.requirements?.threedsChallenge ?: return null
    val encoded = chall.packedSdkChallengeParameters ?: return null

    val params = ChallengeParameters.prepareFromPaymentApiString(
      threeDSRequestorAppURL = THREE_DS_REQUESTOR_APP_URL,
      text = encoded
    )

    lib.metrica.breadcrumb("3ds-Challenge-Prepared", "Sdk 3ds", "state")
    return MainVM.Effect.Require3ds(params, tx!!)
  }

  fun prepareChallengeConfig(params: ChallengeParameters): ChallengeRequestExecutor.Config? {
    lib.metrica.breadcrumb("3ds-Challenge-Config-Requested", "Sdk 3ds", "action")
    return tx?.let { createChallengeArgs(it, params).getOrNull() }
  }

  fun createChallengeArgs(
    transaction: Transaction,
    params: ChallengeParameters,
    timeoutMins: Int = 5
  ): Result<ChallengeRequestExecutor.Config> = runCatching {
    val transformer = MessageTransformerFactory(false).create()
    val reporter = ErrorReporter { e("Error from challange request executor: ${it.message}", it) }
    val keyPair = MyGuavaEphemeralKeyPairGenerator(reporter).generate()
    val creq = ChallengeRequestData(
      acsTransId = params.acsTransactionId ?: "",
      threeDsServerTransId = params.threeDsServerTransactionId ?: "",
      sdkTransId = transaction.sdkTransactionId,
      messageVersion = "2.2.0",
      messageExtensions = listOfNotNull(
        createBridgingExtension(oobAppURLInd = "01", oobContinue = null)
      ).takeIf { params.threeDSRequestorAppURL.orEmpty().isNotEmpty() && transaction.getBrand() == Brand.Mastercard },
      threeDSRequestorAppURL = THREE_DS_REQUESTOR_APP_URL
    )
    ChallengeRequestExecutor.Config(
      messageTransformer = transformer,
      sdkReferenceId = params.acsRefNumber.orEmpty(),
      acsUrl = ACS_URL,
      creqData = creq,
      keys = ChallengeRequestExecutor.Config.Keys(
        keyPair.private.encoded, keyPair.public.encoded
      )
    )
  }

  companion object {
    const val THREE_DS_REQUESTOR_APP_URL = "myguava://flow/3ds/success"
    const val ACS_URL = "https://bank.com"
  }
}