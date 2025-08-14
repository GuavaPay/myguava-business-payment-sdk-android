package com.guavapay.paymentsdk.presentation.platform

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

internal fun Modifier.ime(scroll: ScrollState, extraSpace: Dp = 16.dp): Modifier = composed {
  val view = LocalView.current
  val density = LocalDensity.current
  val scope = rememberCoroutineScope()
  val ime = WindowInsets.ime

  var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }
  var hasFocus by remember { mutableStateOf(false) }

  val imeBottomPx by remember {
    derivedStateOf { ime.getBottom(density) }
  }

  LaunchedEffect(imeBottomPx, hasFocus) {
    if (!hasFocus) return@LaunchedEffect
    val node = coords ?: return@LaunchedEffect
    if (imeBottomPx <= 0) return@LaunchedEffect

    val r: Rect = node.boundsInWindow()
    val windowH = view.height.toFloat()
    val visibleBottom = windowH - imeBottomPx.toFloat()
    val pad = with(density) { extraSpace.toPx() }

    val overlap = r.bottom - (visibleBottom - pad)
    if (overlap > 0f) {
      scope.launch { scroll.animateScrollBy(overlap) }
    }
  }

  onGloballyPositioned { coords = it }.onFocusEvent { hasFocus = it.isFocused }
}