@file:OptIn(FlowPreview::class)

package com.guavapay.paymentsdk.presentation.screens.phone

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.integrations.local.Country
import com.guavapay.paymentsdk.integrations.local.LocalCountries
import com.guavapay.paymentsdk.presentation.navigation.NavigationEvents.Event
import com.guavapay.paymentsdk.presentation.navigation.Route
import com.guavapay.paymentsdk.presentation.navigation.Route.*
import com.guavapay.paymentsdk.presentation.platform.basy
import com.guavapay.paymentsdk.presentation.platform.retrow
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class PhoneVM(private val lib: LibraryUnit, private val handle: SavedStateHandle, private val route: PhoneRoute) : ViewModel() {
  private val x by basy(lib)

  private inline fun launch(
    context: CoroutineContext = EmptyCoroutineContext,
    handler: CoroutineExceptionHandler? = null,
    crossinline block: suspend CoroutineScope.() -> Unit
  ) = x.launch(context, handler, block)

  private val countries = MutableStateFlow<List<Country>>(emptyList())
  private val queryFlow: StateFlow<String> = handle.getStateFlow("query", "")
  private val loading = MutableStateFlow(true)

  private val _state = MutableStateFlow(State())
  val state: StateFlow<State> = _state.asStateFlow()

  init {
    lib.metrica.breadcrumb("Phone-Init", "Sdk UI", "state")

    countries()

    launch {
      queryFlow.collect { q ->
        _state.update { it.copy(searchQuery = q) }
      }
    }

    launch {
      combine(
        countries,
        queryFlow.debounce(100).distinctUntilChanged(),
        loading
      ) { list, qd, isLoading ->
        lib.metrica.breadcrumb("Phone-Filter", "Sdk UI", "action", data = mapOf("query" to qd))

        val t = qd.trim()
        val filtered = if (t.isEmpty()) list else list.filter { c ->
          c.countryName.contains(t, true) ||
            c.phoneCode.contains(t) ||
            c.countryCode.contains(t, true)
        }
        filtered to isLoading
      }.collect { (filtered, isLoading) ->
        _state.update { it.copy(countries = filtered, isLoading = isLoading) }
      }
    }
  }

  private fun countries() {
    launch {
      try {
        loading.update { true }
        lib.metrica.breadcrumb("Phone-Load-Started", "Sdk UI", "state")
        val list = LocalCountries(lib)
        countries.update { list }
        lib.metrica.breadcrumb("Phone-Load-Finished", "Sdk UI", "state", data = mapOf("count" to list.size))
      } catch (e: Exception) {
        countries.update { emptyList() }
        lib.metrica.breadcrumb("Phone-Load-Error", "Sdk UI", "error", data = mapOf("error" to (e.message ?: "")))
        retrow(e)
      } finally {
        loading.update { false }
      }
    }
  }

  val handles = Handles(); inner class Handles {
    fun search(q: String) { handle["query"] = q }
    fun country(country: Country) {
      lib.metrica.breadcrumb("Phone-Selected", "Sdk UI", "action", data = mapOf("country_code" to country.countryCode))
      launch { lib.navigation.fire(Event.PhoneResult(route.requestKey, country.phoneCode, country.countryCode)) }
    }
  }

  data class State(val countries: List<Country> = emptyList(), val searchQuery: String = "", val isLoading: Boolean = false)
}