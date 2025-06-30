package com.guavapay.paymentsdk.presentation.navigation

import java.io.Serializable

internal sealed interface Route : Serializable {
  data object HomeRoute : Route { private fun readResolve(): Any = HomeRoute }
  data class AbortRoute(val throwable: Throwable? = null) : Route
}