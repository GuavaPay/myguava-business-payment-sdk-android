package com.guavapay.paymentsdk.presentation.screens.mainpage._subsections.savedcard._components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme

@Composable internal fun MenuButton(isOpen: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .size(width = 40.dp, height = 48.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Crossfade(
      isOpen,
      animationSpec = tween(200)
    ) { targetState ->
      Icon(
        painter = painterResource(if (targetState) R.drawable.ic_cross else R.drawable.ic_dots),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(24.dp)
      )
    }
  }
}

private class MenuButtonPreviewProvider : PreviewParameterProvider<Boolean> {
  override val values = sequenceOf(true, false)
}

@PreviewLightDark @Composable private fun MenuButtonPreview(@PreviewParameter(MenuButtonPreviewProvider ::class) isChecked: Boolean) {
  PreviewTheme {
    MenuButton(isChecked, {})
  }
}