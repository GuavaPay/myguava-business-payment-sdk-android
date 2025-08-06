@file:OptIn(FlowPreview::class)

package com.guavapay.paymentsdk.presentation.screens.phone

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.integrations.local.Country
import com.guavapay.paymentsdk.integrations.local.LocalCountries
import com.guavapay.paymentsdk.presentation.navigation.Route.PhoneRoute
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class PhoneVM(private val lib: LibraryUnit, private val handle: SavedStateHandle, private val route: PhoneRoute) : ViewModel() {
  private val countries = MutableStateFlow<List<Country>>(emptyList())
  private val queryFlow: StateFlow<String> = handle.getStateFlow("query", "")
  private val loading = MutableStateFlow(true)

  private val _state = MutableStateFlow(State())
  val state: StateFlow<State> = _state.asStateFlow()

  init {
    countries()

    viewModelScope.launch {
      queryFlow.collect { q ->
        _state.update { it.copy(searchQuery = q) }
      }
    }

    viewModelScope.launch {
      combine(
        countries,
        queryFlow.debounce(100).distinctUntilChanged(),
        loading
      ) { list, qd, isLoading ->
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
    viewModelScope.launch {
      try {
        loading.update { true }
        val list = LocalCountries(lib)
        countries.update { list }
      } catch (_: Exception) {
        countries.update { emptyList() }
      } finally {
        loading.update { false }
      }
    }
  }

  val handles = Handles(); inner class Handles {
    fun search(q: String) { handle["query"] = q }
    fun country(country: Country) = route.callback(country.phoneCode, country.countryCode)
  }

  data class State(val countries: List<Country> = emptyList(), val searchQuery: String = "", val isLoading: Boolean = false)
}