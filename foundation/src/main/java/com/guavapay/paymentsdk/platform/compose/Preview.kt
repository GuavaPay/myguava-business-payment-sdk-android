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

@Composable internal fun PreviewTheme(content: @Composable () -> Unit) {
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
    Surface(color = MaterialTheme.colorScheme.surface, content = content)
  }
}