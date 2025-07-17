package com.guavapay.paymentsdk.presentation.looknfeel.threeds

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size.Companion.Zero
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import com.guavapay.paymentsdk.presentation.platform.LocalSizesProvider
import com.guavapay.paymentsdk.presentation.platform.LocalTokensProvider
import com.guavapay.myguava.business.myguava3ds2.init.ui.GButtonCustomization
import com.guavapay.myguava.business.myguava3ds2.init.ui.GLabelCustomization
import com.guavapay.myguava.business.myguava3ds2.init.ui.GTextBoxCustomization
import com.guavapay.myguava.business.myguava3ds2.init.ui.GToolbarCustomization
import com.guavapay.myguava.business.myguava3ds2.init.ui.GUiCustomization
import com.guavapay.myguava.business.myguava3ds2.init.ui.UiCustomization.ButtonType.CANCEL
import com.guavapay.myguava.business.myguava3ds2.init.ui.UiCustomization.ButtonType.CONTINUE
import com.guavapay.myguava.business.myguava3ds2.init.ui.UiCustomization.ButtonType.NEXT
import com.guavapay.myguava.business.myguava3ds2.init.ui.UiCustomization.ButtonType.RESEND
import com.guavapay.myguava.business.myguava3ds2.init.ui.UiCustomization.ButtonType.SELECT
import com.guavapay.myguava.business.myguava3ds2.init.ui.UiCustomization.ButtonType.SUBMIT
import java.lang.String.format

@Composable internal fun threedslaf(ui: GUiCustomization): GUiCustomization {
  val density = LocalDensity.current
  val typography = MaterialTheme.typography
  val colorScheme = MaterialTheme.colorScheme
  val shapes = MaterialTheme.shapes
  val sizes = LocalSizesProvider.current
  val tokens = LocalTokensProvider.current

  val buttonSizes = sizes.button()
  val buttonTokens = tokens.button()
  val textfieldTokens = tokens.textfield()

  val theme = remember(density, typography, colorScheme, shapes, sizes, tokens) {
    ThemeBridgeValues(
      textSizeLarge = typography.titleLarge.fontSize.value.toInt(),
      textSizeNormal = typography.bodyLarge.fontSize.value.toInt(),
      cornerRadiusSmall = shapes.small.topStart.toPx(Zero, density).toInt(),
      cornerRadiusMedium = shapes.medium.topStart.toPx(Zero, density).toInt(),
      buttonHeight = buttonSizes.height.value.toInt(),
      surfaceColor = colorScheme.surface.hex,
      onSurfaceColor = colorScheme.onSurface.hex,
      onSurfaceVariantColor = colorScheme.onSurfaceVariant.hex,
      primaryColor = colorScheme.primary.hex,
      textfieldBorderColor = textfieldTokens.unfocusedIndicatorColor.hex,
      textfieldHintColor = textfieldTokens.unfocusedPlaceholderColor.hex,
      textfieldTextColor = textfieldTokens.focusedTextColor.hex,
      buttonContainerColor = buttonTokens.containerColor.hex,
      buttonContentColor = buttonTokens.contentColor.hex
    )
  }

  return remember(ui, theme) { applyFoundationTheme(ui, theme) }
}

private fun applyFoundationTheme(ui: GUiCustomization, theme: ThemeBridgeValues): GUiCustomization {
  val toolbarCustomization = GToolbarCustomization()
  toolbarCustomization.setBackgroundColor(theme.surfaceColor)
  toolbarCustomization.setTextColor(theme.onSurfaceColor)
  toolbarCustomization.textFontSize = theme.textSizeLarge

  val labelCustomization = GLabelCustomization()
  labelCustomization.setHeadingTextColor(theme.onSurfaceColor)
  labelCustomization.setTextColor(theme.onSurfaceVariantColor)
  labelCustomization.textFontSize = theme.textSizeNormal
  labelCustomization.headingTextFontSize = theme.textSizeNormal

  val textBoxCustomization = GTextBoxCustomization()
  textBoxCustomization.setBorderColor(theme.textfieldBorderColor)
  textBoxCustomization.setHintTextColor(theme.textfieldHintColor)
  textBoxCustomization.setTextColor(theme.textfieldTextColor)
  textBoxCustomization.cornerRadius = theme.cornerRadiusSmall

  val cancelButtonCustomization = GButtonCustomization()
  cancelButtonCustomization.setTextColor(theme.onSurfaceColor)
  cancelButtonCustomization.setBackgroundColor(theme.surfaceColor)
  cancelButtonCustomization.textFontSize = theme.textSizeNormal

  val primaryButtonCustomization = GButtonCustomization()
  primaryButtonCustomization.setBackgroundColor(theme.buttonContainerColor)
  primaryButtonCustomization.setTextColor(theme.buttonContentColor)
  primaryButtonCustomization.cornerRadius = theme.cornerRadiusMedium
  primaryButtonCustomization.height = theme.buttonHeight
  primaryButtonCustomization.textFontSize = theme.textSizeNormal

  val resendButtonCustomization = GButtonCustomization()
  resendButtonCustomization.setTextColor(theme.buttonContainerColor)
  resendButtonCustomization.textFontSize = theme.textSizeNormal

  ui.setAccentColor(theme.primaryColor)
  ui.setToolbarCustomization(toolbarCustomization)
  ui.setLabelCustomization(labelCustomization)
  ui.setTextBoxCustomization(textBoxCustomization)

  ui.setButtonCustomization(cancelButtonCustomization, CANCEL)
  ui.setButtonCustomization(primaryButtonCustomization, NEXT)
  ui.setButtonCustomization(primaryButtonCustomization, CONTINUE)
  ui.setButtonCustomization(primaryButtonCustomization, SUBMIT)
  ui.setButtonCustomization(primaryButtonCustomization, SELECT)
  ui.setButtonCustomization(resendButtonCustomization, RESEND)

  return ui
}

private val Color.hex get() = format("#%06X", 0xFFFFFF and toArgb())

private data class ThemeBridgeValues(
  val textSizeLarge: Int,
  val textSizeNormal: Int,
  val cornerRadiusSmall: Int,
  val cornerRadiusMedium: Int,
  val buttonHeight: Int,
  val surfaceColor: String,
  val onSurfaceColor: String,
  val onSurfaceVariantColor: String,
  val primaryColor: String,
  val textfieldBorderColor: String,
  val textfieldHintColor: String,
  val textfieldTextColor: String,
  val buttonContainerColor: String,
  val buttonContentColor: String
)