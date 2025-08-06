package com.guavapay.paymentsdk.presentation.screens.mainpage._subsections.savedcard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.gateway.banking.PaymentCardCategory.DEBIT
import com.guavapay.paymentsdk.gateway.banking.PaymentCardScheme.AMERICAN_EXPRESS
import com.guavapay.paymentsdk.gateway.banking.PaymentCardScheme.MASTERCARD
import com.guavapay.paymentsdk.gateway.banking.PaymentCardScheme.VISA
import com.guavapay.paymentsdk.presentation.components.atoms.Progress
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import com.guavapay.paymentsdk.presentation.screens.mainpage.MainVM.State.SavedCard
import com.guavapay.paymentsdk.presentation.screens.mainpage._subsections.savedcard._components.SavedCardItem

internal object SavedCardSection {
  data class Actions(
    val onSelect: (String) -> Unit = {},
    val onCvvChange: (String) -> Unit = {},
    val onCvvDone: () -> Unit = {},
    val onDelete: (String) -> Unit = {},
    val onEdit: (String) -> Unit = {},
  )

  @Composable operator fun invoke(
    cards: List<SavedCard>,
    selectedCardId: String?,
    cvvInput: String,
    isLoading: Boolean,
    actions: Actions = Actions()
  ) {
    Column {
      if (isLoading) {
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth()) { Progress(Modifier.align(Alignment.Center)) }
        Spacer(Modifier.height(16.dp))
        return
      }

      val available = remember(cards) { cards.filter { it.isAvailable } }
      val unavailable = remember(cards) { cards.filterNot { it.isAvailable } }

      if (available.isNotEmpty()) {
        CardList(
          list = available,
          selectedCardId = selectedCardId,
          cvvInput = cvvInput,
          enabled = true,
          actions = actions
        )
      }

      if (unavailable.isNotEmpty()) {
        if (available.isNotEmpty()) Spacer(Modifier.height(24.dp))

        Text(
          text = stringResource(R.string.saved_card_not_available),
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(16.dp))

        CardList(
          list = unavailable,
          selectedCardId = null,
          cvvInput = "",
          enabled = false,
          actions = actions
        )
      }
    }
  }

  @Composable private fun CardList(list: List<SavedCard>, selectedCardId: String?, cvvInput: String, enabled: Boolean, actions: Actions) {
    list.fastForEachIndexed { index, card ->
      SavedCardItem(
        maskedPan = card.maskedPan,
        cardName = card.cardName,
        scheme = card.scheme,
        isSelected = enabled && card.id == selectedCardId,
        enabled = enabled && card.isAvailable,
        cvvValue = if (enabled && card.id == selectedCardId) cvvInput else "",
        actions = SavedCardItem.Actions(
          onClick = { actions.onSelect(card.id) },
          onCvc = actions.onCvvChange,
          onCvcDone = actions.onCvvDone,
          onDelete = { actions.onDelete(card.id) },
          onEdit = { actions.onEdit(card.id) }
        )
      )
      if (index < list.lastIndex) Spacer(Modifier.height(12.dp))
    }
  }
}

@PreviewLightDark @Composable private fun SavedCardsSectionPreview() {
  PreviewTheme {
    val cards = listOf(
      SavedCard(id = "1", maskedPan = "*4178", cardName = "Card name", expiryDate = "12/25", scheme = VISA, category = DEBIT, isAvailable = true, cvvLength = 3),
      SavedCard(id = "2", maskedPan = "*4178", cardName = "Card name", expiryDate = "12/25", scheme = MASTERCARD, category = DEBIT, isAvailable = true, cvvLength = 3),
      SavedCard(id = "3", maskedPan = "*4178", cardName = "Card name", expiryDate = "12/25", scheme = AMERICAN_EXPRESS, category = DEBIT, isAvailable = false, cvvLength = 4)
    )
    SavedCardSection(cards = cards, selectedCardId = "1", cvvInput = "123", isLoading = false)
  }
}