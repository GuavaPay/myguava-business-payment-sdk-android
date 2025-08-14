@file:OptIn(ExperimentalComposeUiApi::class)

package com.guavapay.paymentsdk.presentation.screens.cancel

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.gateway.banking.PaymentResult
import com.guavapay.paymentsdk.presentation.components.atoms.Button
import com.guavapay.paymentsdk.presentation.navigation.Route
import com.guavapay.paymentsdk.presentation.navigation.rememberNavBackStack
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import com.guavapay.paymentsdk.presentation.screens.Screen
import com.guavapay.paymentsdk.presentation.screens.cancel.CancelScreen.Actions
import io.sentry.compose.SentryModifier.sentryTag
import io.sentry.compose.SentryTraced
import java.io.Serializable

internal object CancelScreen : Screen<Route.CancelRoute, Actions> {
  data class Actions(
    val finish: (PaymentResult) -> Unit = @JvmSerializableLambda {},
    val close: () -> Unit = @JvmSerializableLambda {}
  ) : Serializable

  @Composable override fun invoke(nav: SnapshotStateList<Route>, route: Route.CancelRoute, actions: Actions) = SentryTraced("cancel-screen") {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
      Text(
        text = stringResource(R.string.cancel_title),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = stringResource(R.string.cancel_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = { actions.close() },
        modifier = Modifier.fillMaxWidth().sentryTag("continue-payment-button"),
        style = Button.primary()
      ) {
        Text(text = stringResource(R.string.continue_payment))
      }

      Spacer(modifier = Modifier.height(12.dp))

      Button(
        onClick = { actions.finish(PaymentResult.Cancel) },
        modifier = Modifier.fillMaxWidth().sentryTag("cancel-payment-button"),
        style = Button.secondary()
      ) {
        Text(text = stringResource(R.string.cancel_payment))
      }
    }
  }

  private fun readResolve(): Any = CancelScreen
}

@PreviewLightDark @Composable private fun CancelScreenPreview() {
  PreviewTheme { CancelScreen(rememberNavBackStack(), Route.CancelRoute, Actions()) }
}