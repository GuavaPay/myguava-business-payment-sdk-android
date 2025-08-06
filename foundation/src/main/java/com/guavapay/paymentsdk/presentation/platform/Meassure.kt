package com.guavapay.paymentsdk.presentation.platform

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp

/**
 * Сраный композ не умеет в переопределение высоты, идиоты джбшные спроектировали defaultMinSize
 * таким образом, что если ты его указываешь, ты фиксируешь размер вьюшки жестко, и меньше размер ты не установишь,
 * почему это дебилизм? Потому что default подразумевает !!ДЕФОЛТНОЕ!! значение, которое !ОЧЕВИДНО! можно переопределить,
 * оно же !ДЕФОЛТНОЕ!, а дефолт который нельзя переопределить, это не дефолт, а fixed (фиксированный, константный, неизменяемый),
 * просто неадекватные люди делали тот модификатор, а ниже спасение для людей, которое работает как DEFAULT, если не передавали
 * свои размеры, то мы используем !ДЕФОЛТ! если передали, то очевидно дефолт переопределяется.
 */
internal fun Modifier.defaultMinSizeIfUnspecified(minWidth: Dp? = null, minHeight: Dp? = null) = then(object : LayoutModifier {
  override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
    val mw = minWidth?.roundToPx()
    val mh = minHeight?.roundToPx()

    val patched = constraints.copy(
      minWidth = if (mw != null && constraints.minWidth == 0) maxOf(constraints.minWidth, mw) else constraints.minWidth,
      minHeight = if (mh != null && constraints.minHeight == 0) maxOf(constraints.minHeight, mh) else constraints.minHeight
    )

    val placeable = measurable.measure(patched)
    val w = maxOf(placeable.width, patched.minWidth)
    val h = maxOf(placeable.height, patched.minHeight)
    return layout(w, h) { placeable.place(0, 0) }
  }
})