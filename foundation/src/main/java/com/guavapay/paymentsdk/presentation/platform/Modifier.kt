package com.guavapay.paymentsdk.presentation.platform

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Stable internal fun Modifier.ime(extra: Dp = 12.dp, useWindowInsets: Boolean = false): Modifier = composed {
  val density = LocalDensity.current
  val ime = WindowInsets.ime
  val visible = ime.getBottom(density) > 0
  val base = if (useWindowInsets) this.windowInsetsPadding(ime) else this.imePadding()
  base.then(Modifier.padding(bottom = if (visible) extra else 0.dp))
}

fun Modifier.imeBottomPadding(extra: Dp = 0.dp): Modifier = composed {
  val density = LocalDensity.current
  val imePx = WindowInsets.ime.getBottom(density)
  val navPx = WindowInsets.navigationBars.getBottom(density)
  val extraPx = if (imePx > 0) with(density) { extra.roundToPx() } else 0
  val totalBottom = max(imePx, navPx) + extraPx
  this.padding(bottom = with(density) { totalBottom.toDp() })
}
