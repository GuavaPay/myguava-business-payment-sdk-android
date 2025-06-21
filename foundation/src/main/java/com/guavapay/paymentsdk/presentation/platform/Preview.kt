package com.guavapay.paymentsdk.presentation.platform

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.guavapay.paymentsdk.gateway.banking.PaymentAmount
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks.AMEX
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks.DINERS_CLUB
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks.DISCOVER
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks.MASTERCARD
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks.VISA
import com.guavapay.paymentsdk.gateway.banking.PaymentCardType.CREDIT
import com.guavapay.paymentsdk.gateway.banking.PaymentCardType.DEBIT
import com.guavapay.paymentsdk.gateway.banking.PaymentInstruments
import com.guavapay.paymentsdk.gateway.banking.PaymentMethod.Card
import com.guavapay.paymentsdk.gateway.launcher.LocalGatewayState
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayState
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale

@Composable internal fun PreviewTheme(content: @Composable () -> Unit) {
  val isDarkTheme = isSystemInDarkTheme()

  val lightColorScheme = lightColorScheme(primary = Color(0xFF2E7D32))
  val darkColorScheme = darkColorScheme(primary = Color(0xFF66BB6A))

  val colorScheme = when {
    isDarkTheme -> darkColorScheme
    else -> lightColorScheme
  }

  val typography = Typography(
    displaySmall = TextStyle(fontWeight = FontWeight.W100, fontSize = 96.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.W600, fontSize = 14.sp)
  )

  val shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
  )

  MaterialTheme(colorScheme = colorScheme, typography = typography, shapes = shapes) {
    Surface(color = MaterialTheme.colorScheme.surface) {
      CompositionLocalProvider(LocalGatewayState provides PaymentGatewayState.demo()) {
        content()
      }
    }
  }
}

private fun PaymentGatewayState.Companion.demo() = PaymentGatewayState(
  merchant = "Demo Store",
  instruments = PaymentInstruments(methods = setOf(Card(networks = setOf(VISA, MASTERCARD, AMEX, DISCOVER, DINERS_CLUB), cardtypes = setOf(CREDIT, DEBIT)))),
  amount = PaymentAmount(BigDecimal("20.00"), Currency.getInstance(Locale.US))
)