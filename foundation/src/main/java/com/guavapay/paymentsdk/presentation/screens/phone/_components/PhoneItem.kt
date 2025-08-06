package com.guavapay.paymentsdk.presentation.screens.phone._components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.integrations.local.Country
import com.guavapay.paymentsdk.presentation.platform.FlagResolver.resolve
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme

@Composable internal fun PhoneItem(country: Country, onClick: (Country) -> Unit, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val flagResId = remember(country.countryCode) { resolve(context, country.countryCode) }

  Row(
    modifier = Modifier
      .clickable { onClick(country) }
      .padding(vertical = 10.dp)
      .then(modifier),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (flagResId != 0) {
      Image(
        painter = painterResource(id = flagResId),
        contentDescription = null,
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape),
        contentScale = ContentScale.Crop
      )
    }

    Text(
      text = country.countryName,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f)
    )

    Text(
      text = country.phoneCode,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@PreviewLightDark @Composable private fun PhoneItemPreview() {
  PreviewTheme {
    PhoneItem(country = Country(countryCode = "GB", countryName = "United Kingdom", phoneCode = "+44"), onClick = {})
  }
}