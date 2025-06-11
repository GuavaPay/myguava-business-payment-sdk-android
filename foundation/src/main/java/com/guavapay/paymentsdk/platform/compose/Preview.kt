package com.guavapay.paymentsdk.platform.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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
import com.guavapay.paymentsdk.gateway.launcher.PaymentGatewayState

internal object PreviewScope

@Composable internal fun PreviewTheme(content: @Composable context(PreviewScope) () -> Unit) {
  val isDarkTheme = isSystemInDarkTheme()

  val lightColorScheme = lightColorScheme(primary = Color(0xFF2E7D32))
  val darkColorScheme = darkColorScheme(primary = Color(0xFF66BB6A))

  val colorScheme = when {
    isDarkTheme -> darkColorScheme
    else -> lightColorScheme
  }

  val typography =
    Typography(
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
      with(PreviewScope) {
        content()
      }
    }
  }
}

context(_: PreviewScope) internal fun PaymentGatewayState.Companion.demo() = PaymentGatewayState(
  merchant = "Demo Store",
  instruments = PaymentInstruments(methods = setOf(Card(networks = setOf(VISA, MASTERCARD, AMEX, DISCOVER, DINERS_CLUB), cardtypes = setOf(CREDIT, DEBIT)))),
  amount = PaymentAmount(java.math.BigDecimal("20.00"), java.util.Currency.getInstance(java.util.Locale.US))
)