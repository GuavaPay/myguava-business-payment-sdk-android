package com.guavapay.paymentsdk.presentation.platform

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.platform.coroutines.CompositeExceptionHandler
import com.guavapay.paymentsdk.platform.function.lazy
import com.guavapay.paymentsdk.rememberLibraryUnit
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

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

internal class ViewModel0 /* Я не придумал лучше названия, но в целом пойдет:). */ (private val vm: ViewModel, private val lib: LibraryUnit) {
  private val handler = CompositeExceptionHandler(lib.coroutine.handlers.logcat, lib.coroutine.handlers.metrica)

  val scope by lazy { CoroutineScope(vm.viewModelScope.coroutineContext + handler) }

  private fun merged(
    external: CoroutineContext,
    extra: CoroutineExceptionHandler?
  ): CoroutineExceptionHandler? {
    val base = scope.coroutineContext[CoroutineExceptionHandler]
    val external = external[CoroutineExceptionHandler]
    val list = listOfNotNull(base, external, extra)
    return when (list.size) {
      0 -> null
      1 -> list.first()
      else -> CompositeExceptionHandler(*list.toTypedArray())
    }
  }

  inline fun launch(
    context: CoroutineContext = EmptyCoroutineContext,
    handler: CoroutineExceptionHandler? = null,
    crossinline block: suspend CoroutineScope.() -> Unit
  ) = run {
    val handlers = context.minusKey(CoroutineExceptionHandler)
    val merged = merged(context, handler)
    scope.launch(lib.coroutine.elements.named + handlers + (merged ?: EmptyCoroutineContext)) {
      block()
    }
  }

  inline fun <T> async(
    context: CoroutineContext = EmptyCoroutineContext,
    crossinline block: suspend CoroutineScope.() -> T
  ) = scope.async(lib.coroutine.elements.named + context) { block() }

  fun <T> Flow<T>.launch() = launchIn(CoroutineScope(scope.coroutineContext + lib.coroutine.elements.named))
}

private class CoroutinifyDelegate(private val vm: ViewModel, private val lib: LibraryUnit) : ReadOnlyProperty<ViewModel, ViewModel0> {
  private var cached: ViewModel0? = null
  @Synchronized override fun getValue(thisRef: ViewModel, property: KProperty<*>) = cached ?: ViewModel0(thisRef, lib).also { cached = it }
}

internal fun coroutinify(vm: ViewModel, lib: LibraryUnit): ReadOnlyProperty<ViewModel, ViewModel0> = CoroutinifyDelegate(vm, lib)