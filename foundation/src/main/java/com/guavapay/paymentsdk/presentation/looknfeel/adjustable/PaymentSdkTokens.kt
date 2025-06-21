package com.guavapay.paymentsdk.presentation.looknfeel.adjustable

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import com.guavapay.paymentsdk.presentation.looknfeel.BrandDisabled
import com.guavapay.paymentsdk.presentation.looknfeel.BrandDisabledDark
import com.guavapay.paymentsdk.presentation.looknfeel.BrandOnDisabled
import com.guavapay.paymentsdk.presentation.looknfeel.BrandOnDisabledDark
import com.guavapay.paymentsdk.presentation.platform.PaymentGatewayTokensProvider

open class PaymentSdkTokens : PaymentGatewayTokensProvider {
  @Composable override fun textfield(): TextFieldColors {
    val isLightTheme = !isSystemInDarkTheme()

    return OutlinedTextFieldDefaults.colors(
      focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
      unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,

      focusedBorderColor = MaterialTheme.colorScheme.primary,
      unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,

      focusedLabelColor = MaterialTheme.colorScheme.primary,
      unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,

      focusedTextColor = MaterialTheme.colorScheme.onSurface,
      unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

      focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
      unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,

      cursorColor = MaterialTheme.colorScheme.primary,

      disabledContainerColor = if (isLightTheme) BrandDisabled.copy(alpha = 0.3f) else BrandDisabledDark.copy(alpha = 0.3f),
      disabledBorderColor = if (isLightTheme) BrandDisabled else BrandDisabledDark,
      disabledTextColor = if (isLightTheme) BrandOnDisabled else BrandOnDisabledDark,
      disabledLabelColor = if (isLightTheme) BrandOnDisabled else BrandOnDisabledDark,
      disabledPlaceholderColor = if (isLightTheme) BrandOnDisabled.copy(alpha = 0.6f) else BrandOnDisabledDark.copy(alpha = 0.6f),

      errorContainerColor = MaterialTheme.colorScheme.surfaceVariant,
      errorBorderColor = MaterialTheme.colorScheme.error,
      errorLabelColor = MaterialTheme.colorScheme.error,
      errorTextColor = MaterialTheme.colorScheme.onSurface,
      errorPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
      errorCursorColor = MaterialTheme.colorScheme.error
    )
  }

  @Composable override fun button(): ButtonColors {
    val isLightTheme = !isSystemInDarkTheme()

    return ButtonDefaults.buttonColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer,
      contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
      disabledContainerColor = if (isLightTheme) BrandDisabled else BrandDisabledDark,
      disabledContentColor = if (isLightTheme) BrandOnDisabled else BrandOnDisabledDark
    )
  }

  @Composable override fun card() = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surface
  )

  @Composable override fun checkbox(): CheckboxColors {
    val isLightTheme = !isSystemInDarkTheme()

    return CheckboxDefaults.colors(
      checkedColor = MaterialTheme.colorScheme.primary,
      checkmarkColor = MaterialTheme.colorScheme.onPrimary,
      uncheckedColor = MaterialTheme.colorScheme.onSurface,
      disabledCheckedColor = if (isLightTheme) BrandDisabled else BrandDisabledDark,
      disabledUncheckedColor = if (isLightTheme) BrandDisabled else BrandDisabledDark,
      disabledIndeterminateColor = if (isLightTheme) BrandDisabled else BrandDisabledDark
    )
  }

  @Composable override fun radio(): RadioButtonColors {
    val isLightTheme = !isSystemInDarkTheme()

    return RadioButtonDefaults.colors(
      selectedColor = MaterialTheme.colorScheme.primary,
      unselectedColor = MaterialTheme.colorScheme.onSurface,
      disabledSelectedColor = if (isLightTheme) BrandDisabled else BrandDisabledDark,
      disabledUnselectedColor = if (isLightTheme) BrandDisabled else BrandDisabledDark
    )
  }

  @Composable override fun switch(): SwitchColors {
    val isLightTheme = !isSystemInDarkTheme()

    return SwitchDefaults.colors(
      checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
      checkedTrackColor = MaterialTheme.colorScheme.primary,
      uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
      uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
      disabledCheckedThumbColor = if (isLightTheme) BrandOnDisabled else BrandOnDisabledDark,
      disabledCheckedTrackColor = if (isLightTheme) BrandDisabled else BrandDisabledDark,
      disabledUncheckedThumbColor = if (isLightTheme) BrandOnDisabled else BrandOnDisabledDark,
      disabledUncheckedTrackColor = if (isLightTheme) BrandDisabled else BrandDisabledDark
    )
  }
}