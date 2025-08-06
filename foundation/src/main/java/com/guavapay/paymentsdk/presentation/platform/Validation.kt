@file:Suppress("OPT_IN_USAGE")

package com.guavapay.paymentsdk.presentation.platform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal enum class FieldState { EMPTY, TYPING, VALID, INVALID_SILENT, INVALID_VISIBLE }
internal const val FIELD_DEBOUNCE_MS = 700L

internal typealias Validator = (String) -> Boolean

internal inline fun softState(value: String, crossinline isValid: Validator): FieldState =
  when {
    value.isBlank() -> FieldState.EMPTY
    isValid(value)  -> FieldState.VALID
    else            -> FieldState.INVALID_SILENT
  }

internal inline fun finalizeState(value: String, crossinline isValid: Validator): FieldState =
  when {
    value.isBlank() -> FieldState.EMPTY
    isValid(value)  -> FieldState.VALID
    else            -> FieldState.INVALID_VISIBLE
  }

internal fun <S, R> collectDebounced(scope: CoroutineScope, source: StateFlow<S>, timeoutMs: Long = FIELD_DEBOUNCE_MS, selector: (S) -> R, block: (R) -> Unit) = scope.launch {
  source.map(selector).distinctUntilChanged().debounce(timeoutMs).collect(block)
}