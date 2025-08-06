package com.guavapay.paymentsdk.presentation.navigation

import java.io.Serializable

internal sealed interface Route : Serializable {
  data object HomeRoute : Route { private fun readResolve(): Any = HomeRoute }
  data class AbortRoute(val throwable: Throwable? = null) : Route
  data object CancelRoute : Route { private fun readResolve(): Any = CancelRoute }
  data class CardRemoveRoute(val cardId: String, val cardName: String = "", val onDeleteConfirmed: (String) -> Unit = @JvmSerializableLambda {}) : Route
  data class CardEditRoute(val cardId: String, val cardName: String = "", val onEditConfirmed: (String, String) -> Unit = @JvmSerializableLambda { _, _ -> }) : Route
  data class ContactRoute(val countryIso: String? = null, val callback: (email: String?, /* or */ phone: String?) -> Unit = @JvmSerializableLambda { _, _ -> }) : Route
  data class PhoneRoute(val callback: (countryCode: String, countryIso: String) -> Unit = @JvmSerializableLambda { _, _ -> }) : Route
}