package com.guavapay.paymentsdk.presentation.components.molecules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.presentation.platform.FlagResolver.resolve
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme

@Composable internal fun CountryPicker(countryCode: String, phoneCode: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val flagResId = remember(countryCode) { resolve(context, countryCode) }

  Surface(
    onClick = onClick,
    modifier = modifier
      .wrapContentWidth()
      .sizeIn(minHeight = 48.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor = MaterialTheme.colorScheme.onSurface,
    shape = MaterialTheme.shapes.small,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (flagResId != 0) {
        Image(
          painter = painterResource(id = flagResId),
          contentDescription = null,
          modifier = Modifier
            .size(24.dp)
            .clip(CircleShape),
          contentScale = ContentScale.Crop
        )
      }

      Text(
        text = phoneCode,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
      )

      Icon(
        painter = painterResource(id = R.drawable.ic_chevron_down),
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

@PreviewLightDark @Composable private fun CountryPickerPreview() {
  PreviewTheme {
    CountryPicker(countryCode = "AZ", phoneCode = "+994", onClick = {})
  }
}