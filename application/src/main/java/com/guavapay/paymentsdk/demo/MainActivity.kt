@file:Suppress("DuplicatedCode")

package com.guavapay.paymentsdk.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.demo.theme.PaymentSdkTheme
import com.guavapay.paymentsdk.gateway.banking.PaymentAmount
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks.AMEX
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks.DINERS_CLUB
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks.DISCOVER
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks.MASTERCARD
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks.UNIONPAY
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks.VISA
import com.guavapay.paymentsdk.gateway.banking.PaymentCardType
import com.guavapay.paymentsdk.gateway.banking.PaymentCardType.CREDIT
import com.guavapay.paymentsdk.gateway.banking.PaymentCardType.DEBIT
import com.guavapay.paymentsdk.gateway.banking.PaymentInstruments
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayCoroutineScope
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayState
import com.guavapay.paymentsdk.gateway.launcher.rememberPaymentGateway
import com.guavapay.paymentsdk.gateway.vendors.googlepay.GPayEnvironment
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Currency
import java.util.Locale
import kotlin.math.roundToInt

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

  var amount by rememberSaveable { mutableIntStateOf(100) }
  var amountText by rememberSaveable { mutableStateOf("100") }

  var visaEnabled by rememberSaveable { mutableStateOf(true) }
  var mastercardEnabled by rememberSaveable { mutableStateOf(true) }
  var amexEnabled by rememberSaveable { mutableStateOf(true) }
  var dinnersEnabled by rememberSaveable { mutableStateOf(false) }
  var unionpayEnabled by rememberSaveable { mutableStateOf(false) }
  var discoverEnabled by rememberSaveable { mutableStateOf(false) }

  var debitEnabled by rememberSaveable { mutableStateOf(true) }
  var creditEnabled by rememberSaveable { mutableStateOf(true) }

  var cardEnabled by rememberSaveable { mutableStateOf(true) }
  var googlePayEnabled by rememberSaveable { mutableStateOf(true) }
  var savedCardEnabled by rememberSaveable { mutableStateOf(false) }

  var usePrebuiltTheme by rememberSaveable { mutableStateOf(false) }

  val state = remember(
    amount, visaEnabled, mastercardEnabled, amexEnabled, dinnersEnabled,
    unionpayEnabled, discoverEnabled, debitEnabled, creditEnabled,
    cardEnabled, googlePayEnabled, savedCardEnabled, usePrebuiltTheme
  ) {
    val selectedSchemes = mutableSetOf<PaymentCardNetworks>().apply {
      if (visaEnabled) add(VISA)
      if (mastercardEnabled) add(MASTERCARD)
      if (amexEnabled) add(AMEX)
      if (unionpayEnabled) add(UNIONPAY)
      if (dinnersEnabled) add(DINERS_CLUB)
      if (discoverEnabled) add(DISCOVER)
    }

    val selectedCardTypes = mutableSetOf<PaymentCardType>().apply {
      if (debitEnabled) add(DEBIT)
      if (creditEnabled) add(CREDIT)
    }

    val selectedMethods = mutableSetOf<PaymentMethod>().apply {
      if (cardEnabled && selectedSchemes.isNotEmpty() && selectedCardTypes.isNotEmpty()) {
        add(
          PaymentMethod.Card(
            networks = selectedSchemes,
            cardtypes = selectedCardTypes
          )
        )
      }
      if (googlePayEnabled && selectedSchemes.isNotEmpty()) {
        add(
          PaymentMethod.GooglePay(
            environment = GPayEnvironment.TEST,
            merchant = "GuavaPay Demo Store",
            networks = selectedSchemes
          )
        )
      }
      if (savedCardEnabled) {
        add(PaymentMethod.SavedCard())
      }
    }

    if (usePrebuiltTheme) {
      PaymentGatewayState(
        merchant = "GuavaPay Demo Store",
        instruments = PaymentInstruments(methods = selectedMethods),
        amount = PaymentAmount(
          value = amount.toBigDecimal(),
          currency = Currency.getInstance(Locale.getDefault()),
        )
      )
    } else {
      PaymentGatewayState(
        decorator = { PaymentSdkTheme(content = it) },
        merchant = "GuavaPay Demo Store",
        instruments = PaymentInstruments(methods = selectedMethods),
        amount = PaymentAmount(
          value = amount.toBigDecimal(),
          currency = Currency.getInstance(Locale.getDefault()),
        )
      )
    }
  }

  val gateway = rememberPaymentGateway(state)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Payment SDK Demo",
      style = MaterialTheme.typography.headlineLarge,
      color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(modifier = Modifier.height(16.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      shape = MaterialTheme.shapes.large
    ) {
      Column(
        modifier = Modifier.padding(20.dp)
      ) {
        Text(
          text = "Theme Selection",
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        CheckboxRow(
          text = "Use prebuilt SDK theme",
          checked = usePrebuiltTheme,
          onCheckedChange = { usePrebuiltTheme = it }
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    AmountConfigurationCard(
      amount = amount,
      amountText = amountText,
      currency = state.amount.currency,
      onAmountChange = { amount = it },
      onAmountTextChange = { amountText = it }
    )

    Spacer(modifier = Modifier.height(16.dp))

    PaymentNetworksCard(
      visaEnabled = visaEnabled,
      mastercardEnabled = mastercardEnabled,
      amexEnabled = amexEnabled,
      isDinnersEnabled = dinnersEnabled,
      unionpayEnabled = unionpayEnabled,
      isDiscoverEnabled = discoverEnabled,
      onVisaChange = { visaEnabled = it },
      onMastercardChange = { mastercardEnabled = it },
      onAmexChange = { amexEnabled = it },
      onDinnersChange = { dinnersEnabled = it },
      onUnionpayChange = { unionpayEnabled = it },
      onDiscoverChange = { discoverEnabled = it }
    )

    Spacer(modifier = Modifier.height(16.dp))

    CardTypesCard(
      debitEnabled = debitEnabled,
      creditEnabled = creditEnabled,
      onDebitChange = { debitEnabled = it },
      onCreditChange = { creditEnabled = it }
    )

    Spacer(modifier = Modifier.height(16.dp))

    PaymentMethodsCard(
      cardEnabled = cardEnabled,
      googlePayEnabled = googlePayEnabled,
      savedCardEnabled = savedCardEnabled,
      onCardChange = { cardEnabled = it },
      onGooglePayChange = { googlePayEnabled = it },
      onSavedCardChange = { savedCardEnabled = it }
    )

    Spacer(modifier = Modifier.height(16.dp))

    CurrentConfigurationCard(state = state)

    Spacer(modifier = Modifier.height(24.dp))

    PaymentButton(
      amount = amount,
      state = state,
      processing = processing,
      cardEnabled = cardEnabled,
      googlePayEnabled = googlePayEnabled,
      savedCardEnabled = savedCardEnabled,
      onPayClick = {
        val scope = PaymentGatewayCoroutineScope()

        processing = true
        scope.launch(CoroutineExceptionHandler { _, e ->
          result = PaymentResult.Failed(PaymentResult.Failed.Error("null", e.message ?: "Unknown error"))
          processing = false
        }) {
          result = gateway.start()
          processing = false
          scope.cancel()
        }
      }
    )

    Spacer(modifier = Modifier.height(24.dp))

    result?.let { result -> PaymentResultCard(result = result) }
  }
}

@Composable private fun AmountConfigurationCard(
  amount: Int,
  amountText: String,
  currency: Currency,
  onAmountChange: (Int) -> Unit,
  onAmountTextChange: (String) -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    shape = MaterialTheme.shapes.large
  ) {
    Column(
      modifier = Modifier.padding(20.dp)
    ) {
      Text(
        text = "Payment Amount",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(16.dp))

      OutlinedTextField(
        value = amountText,
        onValueChange = { newValue ->
          onAmountTextChange(newValue)
          newValue.toIntOrNull()?.let { onAmountChange(it) }
        },
        label = { Text("Amount (${currency.currencyCode})") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Slider: $${amount}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Slider(
        value = amount.toFloat(),
        onValueChange = { newAmount ->
          onAmountChange(newAmount.roundToInt())
          onAmountTextChange(newAmount.roundToInt().toString())
        },
        valueRange = 1f..1000f,
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Composable private fun PaymentNetworksCard(
  visaEnabled: Boolean,
  mastercardEnabled: Boolean,
  amexEnabled: Boolean,
  isDinnersEnabled: Boolean,
  unionpayEnabled: Boolean,
  isDiscoverEnabled: Boolean,
  onVisaChange: (Boolean) -> Unit,
  onMastercardChange: (Boolean) -> Unit,
  onAmexChange: (Boolean) -> Unit,
  onDinnersChange: (Boolean) -> Unit,
  onUnionpayChange: (Boolean) -> Unit,
  onDiscoverChange: (Boolean) -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    shape = MaterialTheme.shapes.large
  ) {
    Column(
      modifier = Modifier.padding(20.dp)
    ) {
      Text(
        text = "Payment Networks",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(12.dp))

      Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
          CheckboxRow("VISA", visaEnabled, onVisaChange)
          CheckboxRow("Mastercard", mastercardEnabled, onMastercardChange)
          CheckboxRow("American Express", amexEnabled, onAmexChange)
        }
        Column(modifier = Modifier.weight(1f)) {
          CheckboxRow("Dinners Club", isDinnersEnabled, onDinnersChange)
          CheckboxRow("UnionPay", unionpayEnabled, onUnionpayChange)
          CheckboxRow("Discover", isDiscoverEnabled, onDiscoverChange)
        }
      }
    }
  }
}

@Composable private fun CardTypesCard(
  debitEnabled: Boolean,
  creditEnabled: Boolean,
  onDebitChange: (Boolean) -> Unit,
  onCreditChange: (Boolean) -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    shape = MaterialTheme.shapes.large
  ) {
    Column(
      modifier = Modifier.padding(20.dp)
    ) {
      Text(
        text = "Card Types",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(12.dp))

      Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
          CheckboxRow("Debit Cards", debitEnabled, onDebitChange)
        }
        Column(modifier = Modifier.weight(1f)) {
          CheckboxRow("Credit Cards", creditEnabled, onCreditChange)
        }
      }
    }
  }
}

@Composable private fun PaymentMethodsCard(
  cardEnabled: Boolean,
  googlePayEnabled: Boolean,
  savedCardEnabled: Boolean,
  onCardChange: (Boolean) -> Unit,
  onGooglePayChange: (Boolean) -> Unit,
  onSavedCardChange: (Boolean) -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    shape = MaterialTheme.shapes.large
  ) {
    Column(
      modifier = Modifier.padding(20.dp)
    ) {
      Text(
        text = "Payment Methods",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(12.dp))

      CheckboxRow("Bank Card", cardEnabled, onCardChange)
      CheckboxRow("Google Pay", googlePayEnabled, onGooglePayChange)
      CheckboxRow("Saved Cards", savedCardEnabled, onSavedCardChange)
    }
  }
}

@Composable private fun CurrentConfigurationCard(state: PaymentGatewayState) {
  val formattedAmount = remember(state.amount) { state.amount.format() }

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ),
    shape = MaterialTheme.shapes.large
  ) {
    Column(
      modifier = Modifier.padding(20.dp)
    ) {
      Text(
        text = "Current Configuration",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = "Merchant: ${state.merchant}",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Amount: $formattedAmount",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Methods: ${state.instruments.methods.size} selected",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable private fun PaymentButton(
  amount: Int,
  state: PaymentGatewayState,
  processing: Boolean,
  cardEnabled: Boolean,
  googlePayEnabled: Boolean,
  savedCardEnabled: Boolean,
  onPayClick: () -> Unit
) {
  val hasValidAmount = amount > 0
  val hasSelectedMethods = cardEnabled || googlePayEnabled || savedCardEnabled
  val hasValidConfiguration = state.instruments.methods.isNotEmpty()
  val isFullyValid = hasValidAmount && hasSelectedMethods && hasValidConfiguration

  Button(
    onClick = onPayClick,
    modifier = Modifier
      .fillMaxWidth()
      .height(56.dp),
    enabled = !processing && isFullyValid,
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
        text = if (isFullyValid) "Pay ${state.amount.format()}" else "Configure Settings",
        style = MaterialTheme.typography.labelLarge
      )
    }
  }

  if (!isFullyValid) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = when {
        !hasValidAmount -> "⚠️ Specify amount greater than 0"
        !hasSelectedMethods -> "⚠️ Select at least one payment method"
        else -> ""
      },
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.error
    )
  }
}

@Composable private fun PaymentResultCard(result: PaymentResult) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    shape = MaterialTheme.shapes.large
  ) {
    Column(
      modifier = Modifier.padding(20.dp)
    ) {
      Text(
        text = "Payment Result",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(12.dp))
      when (result) {
        is PaymentResult.Completed -> {
          Text(
            text = "✅ Success",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
          )
        }

        is PaymentResult.Failed -> {
          Text(
            text = "❌ Error",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Code: ${result.error.code}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Message: ${result.error.message}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        is PaymentResult.Canceled -> {
          Text(
            text = "🚫 Canceled",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
          )
        }
      }
    }
  }
}

@Composable private fun CheckboxRow(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}