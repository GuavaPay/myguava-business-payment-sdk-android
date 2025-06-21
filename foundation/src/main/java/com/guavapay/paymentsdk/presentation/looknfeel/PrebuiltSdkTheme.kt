package com.guavapay.paymentsdk.presentation.looknfeel

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.guavapay.paymentsdk.presentation.platform.LocalSizesProvider
import com.guavapay.paymentsdk.presentation.platform.LocalTokensProvider
import com.guavapay.paymentsdk.presentation.looknfeel.adjustable.PaymentSdkSizes
import com.guavapay.paymentsdk.presentation.looknfeel.adjustable.PaymentSdkTokens

@Composable internal fun PrebuiltSdkTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  val colorScheme = if (darkTheme) PrebuiltDarkColorScheme else PrebuiltLightColorScheme

  CompositionLocalProvider(
    LocalTokensProvider provides PaymentSdkTokens(),
    LocalSizesProvider provides PaymentSdkSizes()
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = PrebuiltSdkTypography,
      shapes = PrebuiltSdkShapes,
      content = content
    )
  }
}