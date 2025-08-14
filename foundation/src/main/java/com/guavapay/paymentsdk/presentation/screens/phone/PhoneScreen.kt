@file:OptIn(ExperimentalComposeUiApi::class)

package com.guavapay.paymentsdk.presentation.screens.phone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.presentation.components.atoms.Progress
import com.guavapay.paymentsdk.presentation.navigation.Route
import com.guavapay.paymentsdk.presentation.navigation.Route.PhoneRoute
import com.guavapay.paymentsdk.presentation.navigation.rememberNavBackStack
import com.guavapay.paymentsdk.presentation.platform.NoActions
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import com.guavapay.paymentsdk.presentation.platform.rememberViewModel
import com.guavapay.paymentsdk.presentation.screens.Screen
import com.guavapay.paymentsdk.presentation.screens.phone._components.PhoneItem
import com.guavapay.paymentsdk.presentation.screens.phone._components.PhoneSearch
import io.sentry.compose.SentryModifier.sentryTag
import io.sentry.compose.SentryTraced

internal object PhoneScreen : Screen<PhoneRoute, NoActions> {
  @Composable override fun invoke(nav: SnapshotStateList<Route>, route: PhoneRoute, actions: NoActions) = SentryTraced("phone-screen") {
    val vm = rememberViewModel(::PhoneVM, route)
    val state = vm.state.collectAsStateWithLifecycle()

    Column {
      IconButton(
        onClick = nav::removeLastOrNull,
        modifier = Modifier.sentryTag("back-button")
      ) {
        Icon(
          painter = painterResource(id = R.drawable.ic_arrow_back),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurface
        )
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp)
          .navigationBarsPadding()
      ) {
        Text(
          modifier = Modifier.padding(horizontal = 16.dp),
          text = stringResource(R.string.select_country),
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.size(16.dp))

        PhoneSearch(
          searchQuery = state.value.searchQuery,
          onSearchQueryChange = vm.handles::search,
          fieldModifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .sentryTag("search-input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
          state.value.isLoading -> {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f),
              contentAlignment = Alignment.Center
            ) {
              Progress()
            }
          }

          state.value.countries.isEmpty() -> {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .weight(1f),
              contentAlignment = Alignment.TopCenter
            ) {
              Text(
                text = stringResource(R.string.nothing_found),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          else -> {
            LazyColumn(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              state = rememberLazyListState(),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              itemsIndexed(
                items = state.value.countries,
                key = { index, c -> c.countryCode }
              ) { index, country ->
                PhoneItem(
                  modifier = Modifier.padding(horizontal = 16.dp).sentryTag("country-item"),
                  country = country,
                  onClick = { selected ->
                    vm.handles.country(selected)
                    nav.removeLastOrNull()
                  }
                )

                if (index < state.value.countries.lastIndex) {
                  HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = Dp.Hairline,
                    color = MaterialTheme.colorScheme.outlineVariant
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  private fun readResolve(): Any = PhoneScreen
}

@PreviewLightDark @Composable private fun PhoneScreenPreview() {
  PreviewTheme {
    PhoneScreen(rememberNavBackStack(), PhoneRoute { _, _ -> }, NoActions)
  }
}