package com.guavapay.paymentsdk.presentation.navigation

import java.io.Serializable

internal sealed interface Route : Serializable {
  data object HomeRoute : Route { private fun readResolve(): Any = HomeRoute }
  data class AbortRoute(val throwable: Throwable? = null) : Route
  data object CancelRoute : Route { private fun readResolve(): Any = CancelRoute }
  data class CardRemoveRoute(val cardId: String, val cardName: String = "") : Route
  data class CardEditRoute(val cardId: String, val cardName: String = "") : Route
  data class ContactRoute(val countryIso: String? = null) : Route
  data object PhoneRoute : Route { private fun readResolve(): Any = PhoneRoute }
}