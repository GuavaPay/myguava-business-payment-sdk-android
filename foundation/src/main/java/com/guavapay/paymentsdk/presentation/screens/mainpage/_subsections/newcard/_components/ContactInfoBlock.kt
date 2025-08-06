package com.guavapay.paymentsdk.presentation.screens.mainpage._subsections.newcard._components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.presentation.components.atoms.Button
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme

internal object ContactInfoBlock {
  data class Payload(val email: String? = null, val phoneNumber: String? = null)

  @Composable operator fun invoke(payload: Payload, onChangeInfoClick: () -> Unit, modifier: Modifier = Modifier) {
    if (payload.email.isNullOrBlank() && payload.phoneNumber.isNullOrBlank()) return

    Card(
      modifier = modifier.fillMaxWidth(),
      shape = MaterialTheme.shapes.medium,
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      border = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
      ),
      elevation = CardDefaults.cardElevation(
        defaultElevation = 0.dp
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = stringResource(R.string.contact_info_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
          )

          Button(
            onClick = onChangeInfoClick,
            modifier = modifier.sizeIn(minWidth = 80.dp, minHeight = 24.dp),
            style = Button.secondary(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
          ) {
            Text(
              text = stringResource(R.string.contact_info_change),
              style = MaterialTheme.typography.labelSmall
            )
          }
        }

        if (payload.email?.isNotBlank() == true) {
          Spacer(modifier = Modifier.height(16.dp))
          ContactInfoItem(label = stringResource(R.string.contact_info_email_label), value = payload.email)
        }

        if (payload.phoneNumber?.isNotBlank() == true) {
          Spacer(modifier = Modifier.height(8.dp))
          ContactInfoItem(label = stringResource(R.string.contact_info_phone_label), value = payload.phoneNumber)
        }
      }
    }
  }

  @Composable private fun ContactInfoItem(label: String, value: String) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        maxLines = 1,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
      )
    }
  }
}

private class ContactInfoBlockPreviewProvider : PreviewParameterProvider<ContactInfoBlock.Payload> {
  override val values = sequenceOf(
    ContactInfoBlock.Payload(email = "e***e@e***.com", phoneNumber = "+44 ********00"),
    ContactInfoBlock.Payload(email = "e***e@e***.com"),
    ContactInfoBlock.Payload(phoneNumber = "+44 ********00"),
  )
}

@PreviewLightDark @Composable private fun ContactInfoCardPreview(@PreviewParameter(ContactInfoBlockPreviewProvider::class) payload: ContactInfoBlock.Payload) {
  PreviewTheme {
    ContactInfoBlock(payload = payload, onChangeInfoClick = {})
  }
}