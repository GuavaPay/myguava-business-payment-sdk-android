package com.guavapay.paymentsdk.presentation.components.compound

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.presentation.components.atomic.CircularProgressIndicator
import com.guavapay.paymentsdk.presentation.components.compound.TextFieldEndContent.Empty
import com.guavapay.paymentsdk.presentation.components.compound.TextFieldEndContent.Icon
import com.guavapay.paymentsdk.presentation.components.compound.TextFieldEndContent.Progress
import com.guavapay.paymentsdk.presentation.platform.LocalSizesProvider
import com.guavapay.paymentsdk.presentation.platform.LocalTokensProvider

private enum class TextFieldEndContent { Progress, Icon, Empty }

@Composable internal fun TextFieldCompound(
  header: String,
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  loading: Boolean = false,
  readOnly: Boolean = false,
  placeholder: String = "",
  endIcon: Painter? = null,
  error: String? = null,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
  onDoneAction: (() -> Unit)? = null,
  singleLine: Boolean = false,
  maxLength: Int? = null,
  onFocusLost: (() -> Unit)? = null,
) {
  val tokens = LocalTokensProvider.current
  val sizes = LocalSizesProvider.current

  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = header,
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurface,
    )

    Spacer(modifier = Modifier.height(4.dp))

    OutlinedTextField(
      value = value,
      onValueChange = { newValue ->
        val filteredValue = if (maxLength != null) {
          newValue.take(maxLength)
        } else {
          newValue
        }
        onValueChange(filteredValue)
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(sizes.textfield().height)
        .then(
          if (onFocusLost != null) {
            Modifier.onFocusChanged { focusState -> if (!focusState.isFocused) onFocusLost() }
          } else Modifier
        ),
      placeholder = {
        Text(
          text = placeholder,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      },
      enabled = enabled,
      readOnly = readOnly,
      textStyle = MaterialTheme.typography.bodyMedium,
      trailingIcon = {
        Crossfade(
          targetState = when {
            loading -> Progress
            endIcon != null -> Icon
            else -> Empty
          },
          animationSpec = tween(300),
          label = remember(TextFieldEndContent::class.simpleName::toString),
        ) { state ->
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(24.dp)
          ) {
            when (state) {
              Progress -> CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
              )

              Icon -> endIcon?.let { icon ->
                Icon(
                  painter = icon,
                  contentDescription = null,
                  modifier = Modifier.size(24.dp),
                  tint = Color.Unspecified,
                )
              }

              Empty -> {}
            }
          }
        }
      },
      isError = error != null,
      visualTransformation = visualTransformation,
      keyboardOptions = keyboardOptions,
      keyboardActions = KeyboardActions(
        onDone = if (keyboardOptions.imeAction == ImeAction.Done && onDoneAction != null) {
          { onDoneAction() }
        } else null
      ),
      singleLine = singleLine,
      shape = MaterialTheme.shapes.small,
      colors = tokens.textfield()
    )

    error?.let { errorText ->
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = errorText,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error
      )
    }
  }
}