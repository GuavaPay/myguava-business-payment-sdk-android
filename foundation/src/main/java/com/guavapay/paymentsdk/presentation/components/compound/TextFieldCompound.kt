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
import com.guavapay.paymentsdk.presentation.platform.LocalSizesProvider
import com.guavapay.paymentsdk.presentation.platform.LocalTokensProvider

@Composable internal fun TextFieldCompound(
  title: String,
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String = "",
  keyboardType: KeyboardType = KeyboardType.Number,
  imeAction: ImeAction = ImeAction.Next,
  onDone: (() -> Unit)? = null,
  onFocusLost: (() -> Unit)? = null,
  maxLength: Int? = null,
  loading: Boolean = false,
  endIcon: Painter? = null,
  error: String? = null,
  singleLine: Boolean = false,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  modifier: Modifier = Modifier
) {
  val tokens = LocalTokensProvider.current
  val sizes = LocalSizesProvider.current

  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = title,
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
      placeholder = {
        Text(
          text = placeholder,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      },
      singleLine = singleLine,
      visualTransformation = visualTransformation,
      trailingIcon = {
        Crossfade(
          targetState = when {
            loading -> "loading"
            endIcon != null -> "icon"
            else -> "empty"
          },
          animationSpec = tween(300),
          label = "TextFieldIcon"
        ) { state ->
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(24.dp)
          ) {
            when (state) { // TODO: Use typed.
              "loading" -> CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
              )
              "icon" -> endIcon?.let { icon ->
                Icon(
                  painter = icon,
                  contentDescription = null,
                  modifier = Modifier.size(24.dp),
                  tint = Color.Unspecified,
                )
              }
            }
          }
        }
      },
      isError = error != null,
      modifier = Modifier
        .fillMaxWidth()
        .height(sizes.textfield().height)
        .then(
          if (onFocusLost != null) {
            Modifier.onFocusChanged { focusState ->
              if (!focusState.isFocused) {
                onFocusLost()
              }
            }
          } else Modifier
        ),
      shape = MaterialTheme.shapes.small,
      colors = tokens.textfield(),
      keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
      keyboardActions = KeyboardActions(
        onDone = if (imeAction == ImeAction.Done && onDone != null) { { onDone() } } else null
      ),
      textStyle = MaterialTheme.typography.bodyMedium
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