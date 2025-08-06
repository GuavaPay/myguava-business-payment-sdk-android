package com.guavapay.paymentsdk.presentation.screens.management

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.R
import com.guavapay.paymentsdk.presentation.navigation.Route.CardEditRoute
import com.guavapay.paymentsdk.presentation.platform.Text
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class CardEditVM(private val lib: LibraryUnit, private val handle: SavedStateHandle, route: CardEditRoute) : ViewModel() {
  private val _state = MutableStateFlow(State(cardName = handle["cardName"] ?: route.cardName))
  val state: StateFlow<State> = _state.asStateFlow()

  val handles = Handles(); inner class Handles {
    fun cn(value: String) {
      val error = if (value.trim().isBlank()) Text.Resource(R.string.card_name_error) else null
      handle["cardName"] = value
      _state.update { s -> s.copy(cardName = value, cardNameError = error) }
    }
  }

  data class State(
    val cardName: String = "",
    val cardNameError: Text? = null,
  )
}