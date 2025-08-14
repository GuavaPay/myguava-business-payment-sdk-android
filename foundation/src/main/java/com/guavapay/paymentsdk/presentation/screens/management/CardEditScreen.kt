@file:OptIn(ExperimentalComposeUiApi::class)

package com.guavapay.paymentsdk.presentation.screens.management

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.presentation.components.atoms.Button
import com.guavapay.paymentsdk.presentation.components.atoms.TextField
import com.guavapay.paymentsdk.presentation.navigation.Route
import com.guavapay.paymentsdk.presentation.navigation.Route.CardEditRoute
import com.guavapay.paymentsdk.presentation.navigation.rememberNavBackStack
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import com.guavapay.paymentsdk.presentation.platform.rememberViewModel
import com.guavapay.paymentsdk.presentation.platform.string
import com.guavapay.paymentsdk.presentation.screens.Screen
import com.guavapay.paymentsdk.presentation.screens.management.CardEditScreen.Actions
import io.sentry.compose.SentryModifier.sentryTag
import io.sentry.compose.SentryTraced
import java.io.Serializable

@OptIn(ExperimentalLayoutApi::class)
internal object CardEditScreen : Screen<CardEditRoute, Actions> {
  data class Actions(val close: () -> Unit = @JvmSerializableLambda {}) : Serializable { private fun readResolve(): Any = Actions() }

  @Composable override fun invoke(nav: SnapshotStateList<Route>, route: CardEditRoute, actions: Actions) = SentryTraced("card-edit-screen") {
    val vm = rememberViewModel(::CardEditVM, route)
    val state = vm.state.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(scroll)
        .padding(horizontal = 16.dp, vertical = 24.dp)
        .imePadding()
        .imeNestedScroll()
    ) {
      Text(
        text = stringResource(R.string.rename_saved_card),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(16.dp))

      TextField(
        value = state.value.cardName,
        onValueChange = vm.handles::cn,
        header = stringResource(R.string.card_name),
        error = state.value.cardNameError?.string(),
        singleLine = true,
        maxLength = 200,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
        onDoneAction = {
          actions.close()
          route.onEditConfirmed(route.cardId, state.value.cardName)
        },
        modifier = Modifier.sentryTag("card-name-input")
      )

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = {
          actions.close()
          route.onEditConfirmed(route.cardId, state.value.cardName)
        },
        modifier = Modifier.fillMaxWidth().sentryTag("save-button"),
        enabled = state.value.cardNameError == null,
        style = Button.primary()
      ) {
        Text(
          text = stringResource(R.string.save),
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
          text = stringResource(R.string.cancel),
          style = MaterialTheme.typography.labelLarge,
        )
      }
    }
  }

  private fun readResolve(): Any = CardEditScreen
}

@PreviewLightDark @Composable private fun CardEditScreenPreview() {
  PreviewTheme { CardEditScreen(rememberNavBackStack(), CardEditRoute("card123", "Homeless Card"), Actions()) }
}