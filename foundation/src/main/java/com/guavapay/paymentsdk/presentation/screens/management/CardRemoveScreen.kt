@file:OptIn(ExperimentalComposeUiApi::class)

package com.guavapay.paymentsdk.presentation.screens.management

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.presentation.components.atoms.Button
import com.guavapay.paymentsdk.presentation.navigation.NavigationEvents.Event
import com.guavapay.paymentsdk.presentation.navigation.Route
import com.guavapay.paymentsdk.presentation.navigation.Route.CardRemoveRoute
import com.guavapay.paymentsdk.presentation.navigation.rememberNavBackStack
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import com.guavapay.paymentsdk.presentation.platform.Text
import com.guavapay.paymentsdk.presentation.platform.annotated
import com.guavapay.paymentsdk.presentation.screens.Screen
import com.guavapay.paymentsdk.presentation.screens.management.CardRemoveScreen.Actions
import com.guavapay.paymentsdk.rememberLibraryUnit
import io.sentry.compose.SentryModifier.sentryTag
import io.sentry.compose.SentryTraced
import kotlinx.coroutines.launch
import java.io.Serializable

internal object CardRemoveScreen : Screen<CardRemoveRoute, Actions> {
  data class Actions(val close: () -> Unit = @JvmSerializableLambda {}) : Serializable { private fun readResolve(): Any = Actions() }

  @Composable override fun invoke(nav: SnapshotStateList<Route>, route: CardRemoveRoute, actions: Actions) = SentryTraced("card-remove-screen") {
    val lib = rememberLibraryUnit()
    val scope = rememberCoroutineScope()

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
      Text(
        text = stringResource(R.string.remove_card_title),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(4.dp))

      val text = remember(route.cardName) {
        Text.ResourceFormat(R.string.remove_card_description, listOf(route.cardName))
      }

      Text(
        text = text.annotated(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = {
          actions.close()
          scope.launch { lib.navigation.fire(Event.ConfirmCardRemove(route.requestKey)) }
        },
        modifier = Modifier.fillMaxWidth().sentryTag("delete-button"),
        style = Button.danger()
      ) {
        Text(
          text = stringResource(R.string.remove_card_delete),
          style = MaterialTheme.typography.labelLarge,
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Button(
        onClick = { actions.close() },
        modifier = Modifier.fillMaxWidth().sentryTag("cancel-button"),
        style = Button.secondary()
      ) {
        Text(
          text = stringResource(R.string.remove_card_cancel),
          style = MaterialTheme.typography.labelLarge,
        )
      }
    }
  }

  private fun readResolve(): Any = CardRemoveScreen
}

@PreviewLightDark @Composable private fun CardRemoveScreenPreview() {
  PreviewTheme { CardRemoveScreen(rememberNavBackStack(), CardRemoveRoute("card123", "Homeless Card *5249", ""), Actions()) }
}