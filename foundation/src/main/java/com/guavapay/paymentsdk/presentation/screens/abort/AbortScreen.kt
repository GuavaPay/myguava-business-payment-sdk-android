package com.guavapay.paymentsdk.presentation.screens.abort

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.presentation.components.atomic.PrimaryButton
import com.guavapay.paymentsdk.presentation.navigation.Route
import com.guavapay.paymentsdk.presentation.navigation.Route.AbortRoute
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import com.guavapay.paymentsdk.presentation.screens.Screen
import com.guavapay.paymentsdk.presentation.screens.abort.AbortScreen.Actions
import java.io.Serializable

internal object AbortScreen : Screen<AbortRoute, Actions> {
  data class Actions(val finish: (Throwable?) -> Unit = {}) : Serializable

  @Composable override operator fun invoke(nav: SnapshotStateList<Route>, route: AbortRoute, actions: Actions) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(24.dp)
        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
        .imePadding()
    ) {
      Box(
        modifier = Modifier
          .width(40.dp)
          .height(4.dp)
          .background(
            MaterialTheme.colorScheme.outline,
            MaterialTheme.shapes.extraSmall
          )
          .align(Alignment.CenterHorizontally)
      )

      Spacer(modifier = Modifier.height(20.dp))

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

      PrimaryButton(
        onClick = { actions.finish(route.throwable) },
        modifier = Modifier.fillMaxWidth()
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
  PreviewTheme { AbortScreen(SnapshotStateList(), AbortRoute(), Actions()) }
}