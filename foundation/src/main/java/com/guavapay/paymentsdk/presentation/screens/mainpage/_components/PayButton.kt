package com.guavapay.paymentsdk.presentation.screens.mainpage._components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.gateway.banking.PaymentKind
import com.guavapay.paymentsdk.presentation.components.atoms.Button
import com.guavapay.paymentsdk.presentation.components.atoms.Progress
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme

@Composable internal fun PayButton(amount: String?, kind: PaymentKind, enabled: Boolean, isLoading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Button(onClick = onClick, enabled = enabled, modifier = modifier, style = Button.primary()) {
    if (isLoading) {
      Progress(
        modifier = Modifier.size(24.dp),
        strokeWidth = 2.dp
      )
    } else {
      val buttonKind = kind.text()
      val text = remember(amount, kind) {
        buildString {
          append(buttonKind)
          if (amount != null) {
            append(" ")
            append(amount)
          }
        }
      }

      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
      )
    }
  }
}

@PreviewLightDark @Composable private fun PayButtonPreview() {
  PreviewTheme {
    PayButton(amount = "1 400 00,99 ₽", kind = PaymentKind.Pay, enabled = true, isLoading = false, onClick = {})
  }
}