package com.guavapay.paymentsdk.presentation.components.atoms

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import androidx.compose.material3.CircularProgressIndicator as Material3CircularProgressIndicator

@Composable internal fun Progress(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface, strokeWidth: Dp = 2.dp, trackColor: Color = ProgressIndicatorDefaults.circularIndeterminateTrackColor, strokeCap: StrokeCap = ProgressIndicatorDefaults.CircularIndeterminateStrokeCap) =
  Material3CircularProgressIndicator(modifier = modifier, color = color, strokeWidth = strokeWidth, trackColor = trackColor, strokeCap = strokeCap)

@PreviewLightDark @Composable private fun ProgressPreview() = PreviewTheme({ Progress() })