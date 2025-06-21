package com.guavapay.paymentsdk.presentation.components.atomic

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator as Material3CircularProgressIndicator

@Composable internal fun CircularProgressIndicator(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface, strokeWidth: Dp = 2.dp) {
  Material3CircularProgressIndicator(modifier = modifier, color = color, strokeWidth = strokeWidth)
}