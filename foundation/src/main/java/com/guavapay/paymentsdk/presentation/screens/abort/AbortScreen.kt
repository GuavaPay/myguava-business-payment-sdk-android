@file:OptIn(ExperimentalComposeUiApi::class)

package com.guavapay.paymentsdk.presentation.screens.abort

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
import com.guavapay.paymentsdk.presentation.components.atoms.Button
import com.guavapay.paymentsdk.presentation.navigation.Route
import com.guavapay.paymentsdk.presentation.navigation.Route.AbortRoute
import com.guavapay.paymentsdk.presentation.navigation.rememberNavBackStack
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import com.guavapay.paymentsdk.presentation.screens.Screen
import com.guavapay.paymentsdk.presentation.screens.abort.AbortScreen.Actions
import io.sentry.compose.SentryModifier.sentryTag
import io.sentry.compose.SentryTraced
import java.io.Serializable

internal object AbortScreen : Screen<AbortRoute, Actions> {
  data class Actions(
    val finish: (Throwable?) -> Unit = @JvmSerializableLambda {},
    val close: () -> Unit = @JvmSerializableLambda {}
  ) : Serializable

  @Composable override operator fun invoke(nav: SnapshotStateList<Route>, route: AbortRoute, actions: Actions) = SentryTraced("abort-screen") {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
      Text(
        text = stringResource(R.string.abort_title),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = stringResource(R.string.abort_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = {
          actions.close()
          actions.finish(route.throwable)
        },
        modifier = Modifier.fillMaxWidth().sentryTag("finish-button"),
        style = Button.primary()
      ) {
        Text(
          text = stringResource(R.string.abort_finish),
          style = MaterialTheme.typography.labelLarge,
        )
      }
    }
  }

  private fun readResolve(): Any = AbortScreen
}

@PreviewLightDark @Composable private fun AbortScreenPreview() {
  PreviewTheme { AbortScreen(rememberNavBackStack(), AbortRoute(), Actions()) }
}