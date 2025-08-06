package com.guavapay.paymentsdk.presentation.components.atoms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.presentation.platform.LocalSizesProvider
import com.guavapay.paymentsdk.presentation.platform.LocalTokensProvider
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import com.guavapay.paymentsdk.presentation.platform.defaultMinSizeIfUnspecified
import androidx.compose.material3.Button as Material3Button

object Button {
  @ConsistentCopyVisibility @Immutable data class Style internal constructor(val colors: ButtonColors, val shape: Shape, val border: BorderStroke? = null)

  @Composable fun primary(): Style {
    val tokens = LocalTokensProvider.current
    return Style(
      colors = tokens.button(),
      shape = MaterialTheme.shapes.small
    )
  }

  @Composable fun secondary(): Style {
    val tokens = LocalTokensProvider.current
    return Style(
      colors = tokens.button().copy(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
      ),
      shape = MaterialTheme.shapes.small,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    )
  }

  @Composable fun danger(): Style {
    val tokens = LocalTokensProvider.current
    return Style(
      colors = tokens.button().copy(
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError
      ),
      shape = MaterialTheme.shapes.small
    )
  }

  @Composable operator fun invoke(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: Style = primary(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
  ) {
    val sizes = LocalSizesProvider.current
    var combined = modifier.defaultMinSizeIfUnspecified(minHeight = sizes.button().height)
    style.border?.let { stroke -> combined = combined.border(stroke, style.shape) }

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
      Material3Button(
        onClick = onClick,
        enabled = enabled,
        modifier = combined,
        shape = style.shape,
        colors = style.colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
      )
    }
  }
}

private enum class Variant { PRIMARY, SECONDARY, DANGER }

private class ButtonPreviewProvider : PreviewParameterProvider<Variant> {
  override val values = sequenceOf(Variant.PRIMARY, Variant.SECONDARY, Variant.DANGER)
}

@PreviewLightDark @Composable private fun ButtonPreview(@PreviewParameter(ButtonPreviewProvider::class) variant: Variant) {
  PreviewTheme {
    val style = when (variant) {
      Variant.PRIMARY -> Button.primary()
      Variant.SECONDARY -> Button.secondary()
      Variant.DANGER -> Button.danger()
    }
    Button(onClick = {}, style = style, modifier = Modifier.fillMaxWidth()) {
      Text(variant.name.lowercase().replaceFirstChar { it.uppercase() })
    }
  }
}


