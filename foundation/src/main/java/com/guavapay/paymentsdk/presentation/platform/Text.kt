package com.guavapay.paymentsdk.presentation.platform

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import android.content.Context

sealed interface Text {
  data class Plain(val value: String) : Text
  data class Resource(@StringRes val id: Int) : Text
  data class ResourceFormat(@StringRes val id: Int, val args: List<Any>) : Text
}

@Composable fun Text.string(context: Context = LocalContext.current) = when (this) {
  is Text.Plain -> value
  is Text.Resource -> context.getString(id)
  is Text.ResourceFormat -> context.getString(id, *args.toTypedArray())
}