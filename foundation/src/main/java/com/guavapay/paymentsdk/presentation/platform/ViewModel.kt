package com.guavapay.paymentsdk.presentation.platform

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.rememberLibraryUnit

internal fun interface ViewModelFactory<T : ViewModel> : Factory {
  fun create(extras: CreationExtras): T
  fun supply() = create(CreationExtras.Empty)

  @Suppress("UNCHECKED_CAST")
  override fun <R : ViewModel> create(modelClass: Class<R>, extras: CreationExtras): R {
    val vm = create(extras)
    check(modelClass.isInstance(vm)) { "Requested ${modelClass.name}, but factory created ${vm::class.java.name}" }
    return vm as R
  }

  override fun <R : ViewModel> create(modelClass: Class<R>) = create(modelClass, CreationExtras.Empty)
}

@Composable internal inline fun <reified T : ViewModel> rememberViewModel(
  noinline ctor: (LibraryUnit, SavedStateHandle) -> T
): T {
  val lib = rememberLibraryUnit()
  return viewModel(factory = ViewModelFactory { extras ->
    val handle = extras.createSavedStateHandle()
    ctor(lib, handle)
  })
}

@Composable internal inline fun <reified T : ViewModel, A> rememberViewModel(
  noinline ctor: (LibraryUnit, SavedStateHandle, A) -> T, a: A
): T {
  val lib = rememberLibraryUnit()
  return viewModel(factory = ViewModelFactory { extras ->
    val handle = extras.createSavedStateHandle()
    ctor(lib, handle, a)
  })
}

@Composable internal inline fun <reified T : ViewModel, A, B> rememberViewModel(
  noinline ctor: (LibraryUnit, SavedStateHandle, A, B) -> T, a: A, b: B
): T {
  val lib = rememberLibraryUnit()
  return viewModel(factory = ViewModelFactory { extras ->
    val handle = extras.createSavedStateHandle()
    ctor(lib, handle, a, b)
  })
}

@Composable internal inline fun <reified T : ViewModel, A, B, C> rememberViewModel(
  noinline ctor: (LibraryUnit, SavedStateHandle, A, B, C) -> T, a: A, b: B, c: C
): T {
  val lib = rememberLibraryUnit()
  return viewModel(factory = ViewModelFactory { extras ->
    val handle = extras.createSavedStateHandle()
    ctor(lib, handle, a, b, c)
  })
}