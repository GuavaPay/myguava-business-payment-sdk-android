@file:OptIn(ExperimentalComposeUiApi::class)

package com.guavapay.paymentsdk.presentation.screens.contact

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.presentation.components.atoms.Button
import com.guavapay.paymentsdk.presentation.components.atoms.TextField
import com.guavapay.paymentsdk.presentation.components.molecules.CountryPicker
import com.guavapay.paymentsdk.presentation.navigation.Route
import com.guavapay.paymentsdk.presentation.navigation.Route.ContactRoute
import com.guavapay.paymentsdk.presentation.navigation.Route.PhoneRoute
import com.guavapay.paymentsdk.presentation.navigation.rememberNavBackStack
import com.guavapay.paymentsdk.presentation.platform.NoActions
import com.guavapay.paymentsdk.presentation.platform.PhoneNumberVisualTransformation
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import com.guavapay.paymentsdk.presentation.platform.remember
import com.guavapay.paymentsdk.presentation.platform.rememberViewModel
import com.guavapay.paymentsdk.presentation.platform.string
import com.guavapay.paymentsdk.presentation.screens.Screen
import io.sentry.compose.SentryModifier.sentryTag
import io.sentry.compose.SentryTraced

internal object ContactScreen : Screen<ContactRoute, NoActions> {
  @Composable override fun invoke(nav: SnapshotStateList<Route>, route: ContactRoute, actions: NoActions) = SentryTraced("contact-screen") {
    val vm = rememberViewModel(::ContactVM, route)
    val state = vm.state.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()

    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    Column {
      IconButton(
        onClick = nav::removeLastOrNull,
        modifier = Modifier.sentryTag("back-button"),
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
          .verticalScroll(scroll)
          .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
          .navigationBarsPadding()
      ) {
        Text(
          text = stringResource(R.string.your_contact_information),
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = stringResource(R.string.contact_description),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
          header = stringResource(R.string.email),
          value = state.value.email,
          onValueChange = vm.handles::email,
          placeholder = stringResource(R.string.enter_your_email),
          error = state.value.emailError?.string(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
          singleLine = true,
          modifier = Modifier.sentryTag("email-input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = stringResource(R.string.phone_number),
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.Top
        ) {
          CountryPicker(
            countryCode = state.value.countryIso,
            phoneCode = state.value.countryCode,
            onClick = { nav.add(PhoneRoute { countryCode, countryIso -> vm.handles.country(countryCode, countryIso) }) },
            modifier = Modifier.sentryTag("country-picker")
          )

          Spacer(modifier = Modifier.width(8.dp))

          val phoneVisual = remember(state.value.countryIso, ::PhoneNumberVisualTransformation)

          TextField(
            value = state.value.phone,
            onValueChange = vm.handles::phone,
            placeholder = stringResource(R.string.phone_number_placeholder),
            error = state.value.phoneError?.string(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
            onDoneAction = {
              vm.handles.onContinue()
              if (vm.state.value.isValid) {
                focusManager.clearFocus()
                keyboard?.hide()
                nav.removeLastOrNull()
              }
            },
            singleLine = true,
            visualTransformation = phoneVisual,
            modifier = Modifier.weight(1f).sentryTag("phone-input")
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            vm.handles.onContinue()
            nav.removeLastOrNull()
          },
          enabled = state.value.isValid,
          modifier = Modifier.fillMaxWidth().sentryTag("continue-button"),
          style = Button.primary()
        ) {
          Text(
            text = stringResource(R.string.continue_text),
            style = MaterialTheme.typography.labelLarge
          )
        }
      }
    }
  }

  private fun readResolve(): Any = ContactScreen
}

@PreviewLightDark @Composable private fun ContactScreenPreview() {
  PreviewTheme {
    ContactScreen(rememberNavBackStack(), ContactRoute(), NoActions)
  }
}