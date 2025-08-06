package com.guavapay.paymentsdk.presentation.screens.mainpage._subsections.savedcard._components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.gateway.banking.PaymentCardScheme
import com.guavapay.paymentsdk.presentation.components.atoms.Radio
import com.guavapay.paymentsdk.presentation.components.atoms.TextField
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme
import com.guavapay.paymentsdk.presentation.platform.rememberCvcPeekState

internal object SavedCardItem {
  data class Actions(
    val onClick: () -> Unit = {},
    val onMenu: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onEdit: () -> Unit = {},
    val onCvc: (String) -> Unit = {},
    val onCvcDone: () -> Unit = {},
  )

  @Composable operator fun invoke(
    maskedPan: String,
    cardName: String,
    cvvValue: String = "",
    scheme: PaymentCardScheme,
    isSelected: Boolean,
    enabled: Boolean = true,
    actions: Actions = Actions(),
    modifier: Modifier = Modifier
  ) {
    var menuOpen by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current

    val logoAlpha by animateFloatAsState(
      targetValue = if (enabled) 1f else 0.6f,
      animationSpec = tween(300),
      label = "LogoAlpha"
    )

    LaunchedEffect(cvvValue, isSelected) {
      if (isSelected && cvvValue.length == scheme.cvc) {
        actions.onCvcDone()
        focus.clearFocus()
        keyboard?.hide()
      }
    }

    Row(
      modifier = modifier.height(48.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(MaterialTheme.colorScheme.surface)
          .border(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            shape = RoundedCornerShape(8.dp)
          )
          .clickable(enabled = enabled) { if (!menuOpen) actions.onClick() }
          .padding(horizontal = 8.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(width = 46.dp, height = 32.dp)
              .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
              .padding(4.dp),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              painter = painterResource(scheme.image),
              contentDescription = null,
              modifier = Modifier.alpha(logoAlpha),
              tint = Color.Unspecified
            )
          }

          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
              text = cardName,
              style = MaterialTheme.typography.bodyMedium,
              color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = maskedPan,
              style = MaterialTheme.typography.bodySmall,
              color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
              maxLines = 1
            )
          }

          Radio(
            selected = isSelected,
            onClick = if (enabled) ({ actions.onClick() }) else null,
            enabled = enabled
          )

          Spacer(Modifier.size(4.dp))
        }
      }

      AnimatedVisibility(
        visible = isSelected && !menuOpen,
        enter = slideInHorizontally(tween(300, delayMillis = 150)) { it } + expandHorizontally(tween(300, delayMillis = 150)),
        exit = slideOutHorizontally(tween(150)) { it } + shrinkHorizontally(tween(150))
      ) {
        val transform = rememberCvcPeekState(maxLength = scheme.cvc)

        TextField(
          modifier = Modifier
            .height(48.dp)
            .width(92.dp),
          value = cvvValue,
          maxLength = scheme.cvc,
          onValueChange = {
            transform.onTextChanged(it)
            actions.onCvc(it)
            if (cvvValue.length == scheme.cvc) {
              focusRequester.freeFocus()
              transform.onFocusLost()
            }
          },
          focusRequester = focusRequester,
          visualTransformation = transform.visualTransformation(),
          enabled = enabled,
          placeholder = stringResource(R.string.initial_newcard_cvv_placeholder),
        )
      }

      AnimatedVisibility(
        visible = menuOpen,
        enter = slideInHorizontally(tween(300, delayMillis = if (isSelected) 150 else 0)) { it } + expandHorizontally(tween(300, delayMillis = if (isSelected) 150 else 0)),
        exit = slideOutHorizontally(tween(150)) { it } + shrinkHorizontally(tween(150))
      ) {
        ContextMenu(
          onDeleteClick = actions.onDelete,
          onEditClick = actions.onEdit
        )
      }

      MenuButton(
        isOpen = menuOpen,
        onClick = {
          menuOpen = !menuOpen
          actions.onMenu()
        }
      )
    }

    LaunchedEffect(isSelected, menuOpen, enabled) {
      if (isSelected && !menuOpen && enabled) focusRequester.requestFocus()
    }
  }
}

@PreviewLightDark @Composable private fun SavedCardItemPreview() {
  PreviewTheme {
    var selected by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      SavedCardItem(maskedPan = "*4178", cardName = "Card name", scheme = PaymentCardScheme.VISA, isSelected = selected == 0, enabled = true, cvvValue = "12")
      SavedCardItem(maskedPan = "*4178", cardName = "Card name", scheme = PaymentCardScheme.MASTERCARD, isSelected = selected == 1, enabled = true)
      SavedCardItem(maskedPan = "*4178", cardName = "Card name", scheme = PaymentCardScheme.AMERICAN_EXPRESS, isSelected = selected == 2, enabled = false)
    }
  }
}