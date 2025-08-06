package com.guavapay.paymentsdk.presentation.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.presentation.looknfeel.PrebuiltSdkTheme

@Composable internal fun PreviewTheme(content: @Composable () -> Unit) {
  PrebuiltSdkTheme {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(16.dp)) { content() }
  }
}
