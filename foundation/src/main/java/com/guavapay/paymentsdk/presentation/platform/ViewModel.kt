package com.guavapay.paymentsdk.presentation.platform

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory

@Immutable internal fun interface ViewModelFactory<T : ViewModel> : Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>) = supply() as T
  fun supply(): T
}