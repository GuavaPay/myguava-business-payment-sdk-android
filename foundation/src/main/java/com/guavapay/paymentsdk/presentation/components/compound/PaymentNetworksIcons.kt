package com.guavapay.paymentsdk.presentation.components.compound

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks

@Composable internal fun PaymentNetworksIcons(networks: List<PaymentCardNetworks>, modifier: Modifier = Modifier) {
  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    itemVerticalAlignment = Alignment.CenterVertically
  ) {
    networks.fastForEach { network ->
      Image(
        painter = painterResource(id = network.imageres),
        contentDescription = null,
        modifier = Modifier.heightIn(max = 32.dp)
      )
    }
  }
}