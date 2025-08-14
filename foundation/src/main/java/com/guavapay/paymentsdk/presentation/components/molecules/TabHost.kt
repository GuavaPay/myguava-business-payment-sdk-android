package com.guavapay.paymentsdk.presentation.components.molecules

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.presentation.components.molecules.TabHost.Node
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import kotlin.math.roundToInt

internal object TabHost {
  data class Node(val text: String, val id: String = text)

  @Composable operator fun invoke(mode: Int, changed: (Int) -> Unit, tabs: List<Node>, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    var width by remember { mutableStateOf(0.dp) }

    val offset by animateOffsetAsState(
      targetValue = Offset(x = with(density) { width.toPx() * mode }, y = 0f),
      animationSpec = tween(300),
      label = "SelectorOffset"
    )

    Box(
      modifier = modifier
        .fillMaxWidth()
        .height(40.dp)
        .background(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.extraLarge)
        .padding(2.dp)
    ) {
      Box(
        modifier = Modifier
          .width(width)
          .height(38.dp)
          .offset { IntOffset(x = offset.x.roundToInt(), y = 0) }
          .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.extraLarge)
      )

      Row(
        modifier = Modifier.fillMaxWidth()
      ) {
        tabs.forEachIndexed { index, tab ->
          Node(
            text = tab.text,
            isSelected = mode == index,
            onClick = { changed(index) },
            modifier = Modifier
              .weight(1f)
              .let { modifier ->
                if (index == 0) {
                  modifier.onSizeChanged { size ->
                    width = with(density) { size.width.toDp() }
                  }
                } else {
                  modifier
                }
              }
          )
        }
      }
    }
  }

  @Composable private fun Node(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val textColor by animateColorAsState(
      targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
      animationSpec = tween(300),
      label = "TextColor"
    )

    Box(
      modifier = modifier
        .height(40.dp)
        .clip(MaterialTheme.shapes.extraLarge)
        .clickable(indication = null, interactionSource = remember(::MutableInteractionSource), onClick = onClick),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
        color = textColor,
        textAlign = TextAlign.Center
      )
    }
  }
}

private class TabHostPreviewParameterProvider : PreviewParameterProvider<List<Node>> {
  override val values = sequenceOf(
    listOf(Node("Saved cards"), Node("New card")),
    listOf(Node("Tab 1"), Node("Tab 2"), Node("Tab 3")),
    listOf(Node("Tab 1"), Node("Tab 2"), Node("Tab 3"), Node("Tab 4")),
  )
}

@Suppress("AssignedValueIsNeverRead") @PreviewLightDark @Composable private fun TabHostPreview(@PreviewParameter(TabHostPreviewParameterProvider::class) tabs: List<Node>) {
  PreviewTheme {
    var mode by remember { mutableIntStateOf(0) }
    TabHost(mode = mode, changed = { mode = it }, tabs = tabs)
  }
}