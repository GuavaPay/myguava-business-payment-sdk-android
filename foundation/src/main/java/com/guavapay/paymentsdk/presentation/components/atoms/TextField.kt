package com.guavapay.paymentsdk.presentation.components.atoms

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.presentation.components.atoms.TextFieldEndContent.Empty
import com.guavapay.paymentsdk.presentation.components.atoms.TextFieldEndContent.Icon
import com.guavapay.paymentsdk.presentation.components.atoms.TextFieldEndContent.Progress
import com.guavapay.paymentsdk.presentation.platform.LocalSizesProvider
import com.guavapay.paymentsdk.presentation.platform.LocalTokensProvider
import com.guavapay.paymentsdk.presentation.platform.PreviewTheme

private enum class TextFieldEndContent { Progress, Icon, Empty }

@Composable internal fun TextField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  fieldModifier: Modifier = Modifier,
  header: String? = null,
  placeholder: String = "",
  enabled: Boolean = true,
  readOnly: Boolean = false,
  loading: Boolean = false,
  leadingIcon: Painter? = null,
  endIcon: Painter? = null,
  endIconTint: Color? = null,
  error: String? = null,
  singleLine: Boolean = false,
  maxLength: Int? = null,
  ignorable: String = "",
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
  keyboardActions: KeyboardActions? = null,
  onDoneAction: (() -> Unit)? = null,
  onFocusLost: (() -> Unit)? = null,
  focusRequester: FocusRequester = FocusRequester.Default,
) {
  val tokens = LocalTokensProvider.current
  val sizes = LocalSizesProvider.current
  val baseModifier = if (header != null) Modifier.fillMaxWidth() else Modifier

  val endState = when {
    loading -> Progress
    endIcon != null -> Icon
    else -> Empty
  }

  val leadingIconSlot = leadingIcon?.let { painter ->
    @Composable {
      Icon(
        painter = painter,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }

  val trailingIconSlot =
    if (endState == Empty) {
      null
    } else {
      @Composable {
        Crossfade(
          targetState = endState,
          animationSpec = tween(300),
          label = remember(TextFieldEndContent::class.simpleName::toString)
        ) { state ->
          Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            when (state) {
              Progress -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
              Icon -> endIcon?.let { p -> Icon(painter = p, contentDescription = null, modifier = Modifier.size(24.dp), tint = endIconTint ?: MaterialTheme.colorScheme.onSurfaceVariant) }
              Empty -> {}
            }
          }
        }
      }
    }

  Column(modifier = Modifier.then(if (header != null) Modifier.fillMaxWidth() else Modifier).then(modifier)) {
    header?.let {
      Text(
        text = it,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(4.dp))
    }

    OutlinedTextField(
      value = value,
      onValueChange = { newValue ->
        val filtered = maxLength?.let { newValue.filterNot { it in ignorable }.take(it) } ?: newValue
        onValueChange(filtered)
      },
      modifier = baseModifier
        .height(sizes.textfield().height)
        .then(
          if (onFocusLost != null) Modifier.onFocusChanged { fs -> if (!fs.isFocused) onFocusLost() } else Modifier
        )
        .focusRequester(focusRequester)
        .then(fieldModifier),
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
      leadingIcon = leadingIconSlot,
      trailingIcon = trailingIconSlot,
      isError = error != null,
      visualTransformation = visualTransformation,
      keyboardOptions = keyboardOptions,
      keyboardActions = keyboardActions ?: KeyboardActions(
        onDone = if (keyboardOptions.imeAction == ImeAction.Done && onDoneAction != null) ({ onDoneAction() }) else null
      ),
      singleLine = singleLine,
      shape = MaterialTheme.shapes.small,
      colors = tokens.textfield()
    )

    error?.let {
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    }
  }
}

private data class TextFieldDesc(val header: String? = null, val value: String = "", val placeholder: String = "", val leadingIconRes: Int? = null, val endIconRes: Int? = null, val loading: Boolean = false, val error: String? = null, val singleLine: Boolean = true)

private class TextFieldCasesProvider : PreviewParameterProvider<TextFieldDesc> {
  override val values: Sequence<TextFieldDesc> = sequenceOf(
    TextFieldDesc(header = null, value = "Partially filled", placeholder = "Type something…", leadingIconRes = null, endIconRes = null),
    TextFieldDesc(header = "Email", value = "john@doe.", placeholder = "name@example.com", endIconRes = R.drawable.ic_chevron_down),
    TextFieldDesc(header = null, value = "Uni", placeholder = "Search country", leadingIconRes = R.drawable.ic_search),
    TextFieldDesc(header = "Card name", value = "My Visa *1234", placeholder = "Enter card name", endIconRes = R.drawable.ic_cross, error = "Название слишком короткое"),
    TextFieldDesc(header = "Lookup", value = "Loading...", placeholder = "Type to search", loading = true)
  )
}

@Suppress("AssignedValueIsNeverRead") @PreviewLightDark @Composable private fun TextFieldPreview(@PreviewParameter(TextFieldCasesProvider::class) args: TextFieldDesc) {
  PreviewTheme {
    var text by remember { mutableStateOf(args.value) }
    TextField(
      value = text,
      onValueChange = { text = it },
      header = args.header,
      placeholder = args.placeholder,
      loading = args.loading,
      leadingIcon = args.leadingIconRes?.let { painterResource(id = it) },
      endIcon = args.endIconRes?.let { painterResource(id = it) },
      error = args.error,
      singleLine = args.singleLine,
      modifier = Modifier,
      fieldModifier = Modifier
    )
  }
}