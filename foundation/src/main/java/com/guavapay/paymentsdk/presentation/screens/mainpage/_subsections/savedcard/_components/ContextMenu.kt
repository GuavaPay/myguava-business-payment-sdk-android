package com.guavapay.paymentsdk.presentation.screens.mainpage._subsections.savedcard._components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import io.sentry.compose.SentryModifier.sentryTag

@Composable internal fun ContextMenu(onDeleteClick: () -> Unit, onEditClick: () -> Unit, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Box(
      modifier = Modifier
        .size(width = 40.dp, height = 46.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colorScheme.surface)
        .border(1.dp, color = MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(8.dp))
        .clickable { onEditClick() }
        .sentryTag("edit-card-button"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        modifier = Modifier.size(24.dp),
        painter = painterResource(R.drawable.ic_pencil),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface
      )
    }

    Box(
      modifier = Modifier
        .size(width = 40.dp, height = 46.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colorScheme.error)
        .clickable { onDeleteClick() }
        .sentryTag("delete-card-button"),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        modifier = Modifier.size(24.dp),
        painter = painterResource(R.drawable.ic_trash),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onError
      )
    }
  }
}

@PreviewLightDark @Composable private fun ContextMenuPreview() {
  PreviewTheme {
    ContextMenu(onDeleteClick = {}, onEditClick = {})
  }
}