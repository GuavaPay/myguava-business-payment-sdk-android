package com.guavapay.paymentsdk.presentation.platform

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml

internal sealed interface Text {
  data class Plain(val value: String) : Text
  data class Resource(@StringRes val id: Int) : Text
  data class ResourceFormat(@StringRes val id: Int, val args: List<Any>) : Text
}

@Composable @ReadOnlyComposable internal fun Text.string(context: Context = LocalContext.current) = when (this) {
  is Text.Plain -> value
  is Text.Resource -> context.getString(id)
  is Text.ResourceFormat -> context.getString(id, *args.toTypedArray())
}

@Composable internal fun Text.annotated(context: Context = LocalContext.current): AnnotatedString {
  val raw = when (this) {
    is Text.Plain -> value
    is Text.Resource -> context.getString(id)
    is Text.ResourceFormat -> context.getString(id, *args.toTypedArray())
  }
  return remember(context, raw) { AnnotatedString.fromHtml(raw) }
}