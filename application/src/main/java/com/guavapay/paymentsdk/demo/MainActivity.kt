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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.guavapay.paymentsdk.demo.theme.PaymentSdkTheme
import com.guavapay.paymentsdk.gateway.banking.PaymentCardCategory
import com.guavapay.paymentsdk.gateway.banking.PaymentCardCategory.CREDIT
import com.guavapay.paymentsdk.gateway.banking.PaymentCardCategory.DEBIT
import com.guavapay.paymentsdk.gateway.banking.PaymentCardCategory.PREPAID
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetwork
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetwork.AMERICAN_EXPRESS
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetwork.DINERS_CLUB
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetwork.MASTERCARD
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetwork.UNIONPAY
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetwork.VISA
import com.guavapay.paymentsdk.gateway.banking.PaymentCircuit
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayCoroutineScope
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayPayload
import com.guavapay.paymentsdk.gateway.launcher.rememberPaymentGateway
import kotlinx.coroutines.launch

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

  var circuit by rememberSaveable { mutableStateOf(PaymentCircuit.Sandbox) }
  var token by rememberSaveable { mutableStateOf("sk_sand_ZwYAAAAAAADSPUyUhFygHZTn+TH/bDZ6RsZljO3+qhAf+Ed1HPA4jQ") }
  var sum by rememberSaveable { mutableStateOf("100.25") }
  var currency by rememberSaveable { mutableStateOf("GBP") }

  var phoneNumber by rememberSaveable { mutableStateOf("") }
  var email by rememberSaveable { mutableStateOf("") }

  var visaEnabled by rememberSaveable { mutableStateOf(true) }
  var mastercardEnabled by rememberSaveable { mutableStateOf(true) }
  var amexEnabled by rememberSaveable { mutableStateOf(true) }
  var dinnersEnabled by rememberSaveable { mutableStateOf(true) }
  var unionpayEnabled by rememberSaveable { mutableStateOf(true) }

  var debitEnabled by rememberSaveable { mutableStateOf(true) }
  var creditEnabled by rememberSaveable { mutableStateOf(true) }
  var prepaidEnabled by rememberSaveable { mutableStateOf(true) }

  var googlePayEnabled by rememberSaveable { mutableStateOf(false) }
  var savedCardEnabled by rememberSaveable { mutableStateOf(false) }

  val gateway = paymentState?.let { rememberPaymentGateway(it) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(24.dp)
  ) {
    ApiConfigurationCard(
      circuit = circuit,
      token = token,
      sum = sum,
      currency = currency,
      phoneNumber = phoneNumber,
      email = email,
      onCircuitChange = { circuit = it },
      onTokenChange = { token = it },
      onSumChange = { newValue ->
        val filtered = newValue.filter { it.isDigit() || it == '.' }.trim()
        sum = filtered
      },
      onCurrencyChange = { currency = it },
      onPhoneNumberChange = { phoneNumber = it.trim() },
      onEmailChange = { email = it.trim() }
    )

    Spacer(modifier = Modifier.height(16.dp))

    CompactPaymentOptionsCard(
      visaEnabled = visaEnabled,
      mastercardEnabled = mastercardEnabled,
      amexEnabled = amexEnabled,
      dinnersEnabled = dinnersEnabled,
      unionpayEnabled = unionpayEnabled,
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
            val response = createOrder(getBaseUrlFor(circuit), token, sum.toDouble(), currency, phoneNumber, email)

            val selectedSchemes = mutableSetOf<PaymentCardNetwork>().apply {
              if (visaEnabled) add(VISA)
              if (mastercardEnabled) add(MASTERCARD)
              if (amexEnabled) add(AMERICAN_EXPRESS)
              if (unionpayEnabled) add(UNIONPAY)
              if (dinnersEnabled) add(DINERS_CLUB)
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
              categories = selectedCardTypes.toSet(),
              circuit = circuit,
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
  circuit: PaymentCircuit,
  token: String,
  sum: String,
  currency: String,
  phoneNumber: String,
  email: String,
  onCircuitChange: (PaymentCircuit) -> Unit,
  onTokenChange: (String) -> Unit,
  onSumChange: (String) -> Unit,
  onCurrencyChange: (String) -> Unit,
  onPhoneNumberChange: (String) -> Unit,
  onEmailChange: (String) -> Unit
) {
  var circuitExpanded by remember { mutableStateOf(false) }
  var currencyExpanded by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = MaterialTheme.shapes.large
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Box {
        OutlinedTextField(
          value = circuit.name,
          onValueChange = { },
          label = { Text("Environment") },
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
            .clickable { circuitExpanded = true }
        )
        DropdownMenu(
          expanded = circuitExpanded,
          onDismissRequest = { circuitExpanded = false }
        ) {
          PaymentCircuit.entries.forEach { circuit ->
            DropdownMenuItem(
              enabled = circuit != PaymentCircuit.Production,
              text = { Text(circuit.name) },
              onClick = {
                onCircuitChange(circuit)
                circuitExpanded = false
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

        Box(modifier = Modifier.weight(1f)) {
          OutlinedTextField(
            value = currency,
            onValueChange = { },
            label = { Text("Currency") },
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors().copy(unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface),
            trailingIcon = { Text(text = "▼", style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.fillMaxWidth()
          )
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .clickable { currencyExpanded = true }
          )
          DropdownMenu(
            expanded = currencyExpanded,
            onDismissRequest = { currencyExpanded = false }
          ) {
            TOP_CURRENCIES.fastForEach { (code, name) ->
              DropdownMenuItem(
                text = { Text("$code - $name") },
                onClick = {
                  onCurrencyChange(code)
                  currencyExpanded = false
                }
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = phoneNumber,
        onValueChange = onPhoneNumberChange,
        label = { Text("Phone Number") },
        placeholder = { Text("+1234567890") },
        colors = OutlinedTextFieldDefaults.colors().copy(unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        placeholder = { Text("user@example.com") },
        colors = OutlinedTextFieldDefaults.colors().copy(unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable private fun CompactPaymentOptionsCard(
  visaEnabled: Boolean,
  mastercardEnabled: Boolean,
  amexEnabled: Boolean,
  dinnersEnabled: Boolean,
  unionpayEnabled: Boolean,
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
      PaymentSection(title = "Payment Schemes") {
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
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .2f))
      Spacer(modifier = Modifier.height(12.dp))

      PaymentSection(title = "Product card categories") {
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

      PaymentSection(title = "Payment methods") {
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
          enabled = false, // TODO: remove this.
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
@Composable private fun PaymentSection(title: String, content: @Composable () -> Unit) {
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
          result.payment?.let { transactionResult ->
            Text(
              text = "$transactionResult",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
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

        is PaymentResult.Declined -> {
          Text(
            text = "🚫 Payment Declined",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
          )
          result.payment?.let { transactionResult ->
            Text(
              text = "$transactionResult",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }
  }
}