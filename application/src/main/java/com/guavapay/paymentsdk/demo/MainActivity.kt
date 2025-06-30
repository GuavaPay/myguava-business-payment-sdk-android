@file:Suppress("DuplicatedCode")

package com.guavapay.paymentsdk.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.demo.theme.PaymentSdkTheme
import com.guavapay.paymentsdk.gateway.banking.PaymentCardCategory
import com.guavapay.paymentsdk.gateway.banking.PaymentCardCategory.CREDIT
import com.guavapay.paymentsdk.gateway.banking.PaymentCardCategory.DEBIT
import com.guavapay.paymentsdk.gateway.banking.PaymentCardCategory.PREPAID
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetwork
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetwork.AMEX
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetwork.DINERS_CLUB
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetwork.DISCOVER
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetwork.MASTERCARD
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetwork.UNIONPAY
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetwork.VISA
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayCoroutineScope
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayPayload
import com.guavapay.paymentsdk.gateway.launcher.rememberPaymentGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.random.Random

private val baseUrls = listOf("https://sandbox-pgw.myguava.com", "https://cardium-cpg-dev.guavapay.com", "https://api-pgw.myguava.com")

@Serializable data class OrderRequest(val totalAmount: TotalAmount, val referenceNumber: String)
@Serializable data class TotalAmount(val baseUnits: Double, val currency: String)
@Serializable data class OrderResponse(val order: Order)
@Serializable data class Order(val id: String, val referenceNumber: String, val status: String, val totalAmount: ResponseTotalAmount, val expirationDate: String, val sessionToken: String)
@Serializable data class ResponseTotalAmount(val baseUnits: Double, val currency: String, val localized: String, val minorSubunits: Long)

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      PaymentSdkTheme {
        Surface(
          modifier = Modifier.fillMaxSize().systemBarsPadding(),
          color = MaterialTheme.colorScheme.background,
          content = { PaymentDemoContent() }
        )
      }
    }
  }
}

@Composable private fun PaymentDemoContent() {
  var result by rememberSaveable { mutableStateOf<PaymentResult?>(null) }
  var processing by rememberSaveable { mutableStateOf(false) }
  var paymentState by rememberSaveable { mutableStateOf<PaymentGatewayPayload?>(null) }

  var baseUrl by rememberSaveable { mutableStateOf("https://sandbox-pgw.myguava.com/") }
  var token by rememberSaveable { mutableStateOf("sk_sand_ZwYAAAAAAADSPUyUhFygHZTn+TH/bDZ6RsZljO3+qhAf+Ed1HPA4jQ") }
  var sum by rememberSaveable { mutableStateOf("100.25") }
  var currency by rememberSaveable { mutableStateOf("GBP") }

  var visaEnabled by rememberSaveable { mutableStateOf(true) }
  var mastercardEnabled by rememberSaveable { mutableStateOf(true) }
  var amexEnabled by rememberSaveable { mutableStateOf(true) }
  var dinnersEnabled by rememberSaveable { mutableStateOf(true) }
  var unionpayEnabled by rememberSaveable { mutableStateOf(true) }
  var discoverEnabled by rememberSaveable { mutableStateOf(true) }

  var debitEnabled by rememberSaveable { mutableStateOf(true) }
  var creditEnabled by rememberSaveable { mutableStateOf(true) }
  var prepaidEnabled by rememberSaveable { mutableStateOf(true) }

  var googlePayEnabled by rememberSaveable { mutableStateOf(false) }
  var savedCardEnabled by rememberSaveable { mutableStateOf(true) }

  val locale = Locale.current.platformLocale
  val gateway = paymentState?.let { rememberPaymentGateway(it) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(24.dp)
  ) {
    ApiConfigurationCard(
      baseUrl = baseUrl,
      token = token,
      sum = sum,
      currency = currency,
      onBaseUrlChange = { baseUrl = it },
      onTokenChange = { token = it },
      onSumChange = { sum = it },
      onCurrencyChange = { currency = it }
    )

    Spacer(modifier = Modifier.height(16.dp))

    CompactPaymentOptionsCard(
      visaEnabled = visaEnabled,
      mastercardEnabled = mastercardEnabled,
      amexEnabled = amexEnabled,
      dinnersEnabled = dinnersEnabled,
      unionpayEnabled = unionpayEnabled,
      discoverEnabled = discoverEnabled,
      debitEnabled = debitEnabled,
      creditEnabled = creditEnabled,
      prepaidEnabled = prepaidEnabled,
      googlePayEnabled = googlePayEnabled,
      savedCardEnabled = savedCardEnabled,
      onVisaChange = { visaEnabled = it },
      onMastercardChange = { mastercardEnabled = it },
      onAmexChange = { amexEnabled = it },
      onDinnersChange = { dinnersEnabled = it },
      onUnionpayChange = { unionpayEnabled = it },
      onDiscoverChange = { discoverEnabled = it },
      onDebitChange = { debitEnabled = it },
      onCreditChange = { creditEnabled = it },
      onPrepaidChange = { prepaidEnabled = it },
      onGooglePayChange = { googlePayEnabled = it },
      onSavedCardChange = { savedCardEnabled = it }
    )

    Spacer(modifier = Modifier.weight(1f))

    result?.let { result ->
      CompactPaymentResultCard(result = result)
      Spacer(modifier = Modifier.height(16.dp))
    }

    Button(
      onClick = {
        val scope = PaymentGatewayCoroutineScope()

        scope.launch {
          processing = true
          runCatching {
            val response = createOrder(baseUrl, token, sum.toDouble(), currency)

            val selectedSchemes = mutableSetOf<PaymentCardNetwork>().apply {
              if (visaEnabled) add(VISA)
              if (mastercardEnabled) add(MASTERCARD)
              if (amexEnabled) add(AMEX)
              if (unionpayEnabled) add(UNIONPAY)
              if (dinnersEnabled) add(DINERS_CLUB)
              if (discoverEnabled) add(DISCOVER)
            }

            val selectedCardTypes = mutableSetOf<PaymentCardCategory>().apply {
              if (debitEnabled) add(DEBIT)
              if (creditEnabled) add(CREDIT)
              if (prepaidEnabled) add(PREPAID)
            }

            val selectedMethods = mutableSetOf<PaymentMethod>().apply {
              if (selectedSchemes.isNotEmpty() && selectedCardTypes.isNotEmpty()) {
                add(PaymentMethod.Card())
              }
              if (googlePayEnabled && selectedSchemes.isNotEmpty()) {
                add(PaymentMethod.GooglePay)
              }
              if (savedCardEnabled) {
                add(PaymentMethod.SavedCard())
              }
            }

            val state = PaymentGatewayPayload(
              response.order.id,
              response.order.sessionToken,
              methods = selectedMethods.toSet(),
              networks = selectedSchemes.toSet(),
              categories = selectedCardTypes.toSet()
            )

            paymentState = state
          }.onFailure {
            result = PaymentResult.Failed(it)
            processing = false
          }
        }
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .padding(top = 16.dp),
      enabled = !processing,
      shape = MaterialTheme.shapes.medium
    ) {
      if (processing) {
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          strokeWidth = 2.dp,
          color = MaterialTheme.colorScheme.onSurface,
          strokeCap = StrokeCap.Round,
        )
      } else {
        Text(
          text = "Proceed to Pay",
          style = MaterialTheme.typography.labelLarge
        )
      }
    }
  }

  LaunchedEffect(paymentState) {
    paymentState?.let { state ->
      val scope = PaymentGatewayCoroutineScope()
      scope.launch {
        runCatching {
          result = gateway?.start()
        }.onFailure {
          result = PaymentResult.Failed(it)
        }
        processing = false
        paymentState = null
      }
    }
  }
}

@Composable private fun ApiConfigurationCard(
  baseUrl: String,
  token: String,
  sum: String,
  currency: String,
  onBaseUrlChange: (String) -> Unit,
  onTokenChange: (String) -> Unit,
  onSumChange: (String) -> Unit,
  onCurrencyChange: (String) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = MaterialTheme.shapes.large
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Box {
        val baseUrl = remember(baseUrl) { baseUrl.substringAfterLast("://").removeSuffix("/") }
        OutlinedTextField(
          value = baseUrl,
          onValueChange = { },
          label = { Text("Base URL") },
          readOnly = true,
          colors = OutlinedTextFieldDefaults.colors().copy(unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface),
          trailingIcon = { Text(text = "▼", style = MaterialTheme.typography.bodyMedium) },
          modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        )
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(0.dp)
            .clickable { expanded = true }
        )
        DropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false }
        ) {
          baseUrls.forEach { url ->
            val url = remember(url) { url.substringAfterLast("://") }

            DropdownMenuItem(
              text = { Text(url) },
              onClick = {
                onBaseUrlChange(url)
                expanded = false
              }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = token,
        onValueChange = onTokenChange,
        label = { Text("Token") },
        colors = OutlinedTextFieldDefaults.colors().copy(unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = sum,
          onValueChange = onSumChange,
          label = { Text("Sum") },
          colors = OutlinedTextFieldDefaults.colors().copy(unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.weight(2f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
          value = currency,
          onValueChange = onCurrencyChange,
          colors = OutlinedTextFieldDefaults.colors().copy(unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface),
          label = { Text("Currency") },
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompactPaymentOptionsCard(
  visaEnabled: Boolean,
  mastercardEnabled: Boolean,
  amexEnabled: Boolean,
  dinnersEnabled: Boolean,
  unionpayEnabled: Boolean,
  discoverEnabled: Boolean,
  debitEnabled: Boolean,
  creditEnabled: Boolean,
  prepaidEnabled: Boolean,
  googlePayEnabled: Boolean,
  savedCardEnabled: Boolean,
  onVisaChange: (Boolean) -> Unit,
  onMastercardChange: (Boolean) -> Unit,
  onAmexChange: (Boolean) -> Unit,
  onDinnersChange: (Boolean) -> Unit,
  onUnionpayChange: (Boolean) -> Unit,
  onDiscoverChange: (Boolean) -> Unit,
  onDebitChange: (Boolean) -> Unit,
  onCreditChange: (Boolean) -> Unit,
  onPrepaidChange: (Boolean) -> Unit,
  onGooglePayChange: (Boolean) -> Unit,
  onSavedCardChange: (Boolean) -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = MaterialTheme.shapes.large
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      PaymentSection(title = "Payment Networks") {
        FilterChip(
          selected = visaEnabled,
          onClick = { onVisaChange(!visaEnabled) },
          label = { Text("Visa") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.inverseOnSurface,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        FilterChip(
          selected = mastercardEnabled,
          onClick = { onMastercardChange(!mastercardEnabled) },
          label = { Text("Mastercard") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.inverseOnSurface,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        FilterChip(
          selected = amexEnabled,
          onClick = { onAmexChange(!amexEnabled) },
          label = { Text("Amex") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.inverseOnSurface,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        FilterChip(
          selected = dinnersEnabled,
          onClick = { onDinnersChange(!dinnersEnabled) },
          label = { Text("Diners") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.inverseOnSurface,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        FilterChip(
          selected = unionpayEnabled,
          onClick = { onUnionpayChange(!unionpayEnabled) },
          label = { Text("UnionPay") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.inverseOnSurface,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        FilterChip(
          selected = discoverEnabled,
          onClick = { onDiscoverChange(!discoverEnabled) },
          label = { Text("Discover") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.inverseOnSurface,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .2f))
      Spacer(modifier = Modifier.height(12.dp))

      PaymentSection(title = "Card Types") {
        FilterChip(
          selected = debitEnabled,
          onClick = { onDebitChange(!debitEnabled) },
          label = { Text("Debit") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.inverseOnSurface,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        FilterChip(
          selected = creditEnabled,
          onClick = { onCreditChange(!creditEnabled) },
          label = { Text("Credit") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.inverseOnSurface,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        FilterChip(
          selected = prepaidEnabled,
          onClick = { onPrepaidChange(!prepaidEnabled) },
          label = { Text("Prepaid") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.inverseOnSurface,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .2f))
      Spacer(modifier = Modifier.height(12.dp))

      PaymentSection(title = "Methods") {
        FilterChip(
          selected = googlePayEnabled,
          onClick = { onGooglePayChange(!googlePayEnabled) },
          label = { Text("Google Pay") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.inverseOnSurface,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
        FilterChip(
          selected = savedCardEnabled,
          onClick = { onSavedCardChange(!savedCardEnabled) },
          label = { Text("Saved Card") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.inverseOnSurface,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PaymentSection(
  title: String,
  content: @Composable () -> Unit
) {
  Text(
    text = title,
    style = MaterialTheme.typography.titleMedium,
    color = MaterialTheme.colorScheme.onSurface
  )
  Spacer(modifier = Modifier.height(8.dp))
  FlowRow(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    content()
  }
}

@Composable private fun CompactPaymentResultCard(result: PaymentResult) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = MaterialTheme.shapes.large
  ) {
    Column(modifier = Modifier.padding(20.dp)) {
      when (result) {
        is PaymentResult.Completed -> {
          Text(
            text = "✅ Payment Completed",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
          )
        }

        is PaymentResult.Failed -> {
          Text(
            text = "❌ Payment Failed",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
          )
          Text(
            text = result.throwable.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        is PaymentResult.Canceled -> {
          Text(
            text = "🚫 Payment Canceled",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
          )
        }
      }
    }
  }
}

private suspend fun createOrder(baseUrl: String, token: String, amount: Double, currency: String): OrderResponse {
  return withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val json = Json { ignoreUnknownKeys = true }

    val request = OrderRequest(
      totalAmount = TotalAmount(baseUnits = amount, currency = currency),
      referenceNumber = Random.nextInt(1000, 9999).toString()
    )

    val requestBody = json.encodeToString(request)
      .toRequestBody("application/json".toMediaType())

    val httpRequest = Request.Builder()
      .url("$baseUrl/order")
      .addHeader("Authorization", "Bearer $token")
      .addHeader("Content-Type", "application/json")
      .post(requestBody)
      .build()

    val response = client.newCall(httpRequest).execute()
    if (!response.isSuccessful) {
      throw Exception("HTTP ${response.code}: ${response.message}")
    }

    val responseBody = response.body?.string() ?: throw Exception("Empty response")
    json.decodeFromString<OrderResponse>(responseBody)
  }
}