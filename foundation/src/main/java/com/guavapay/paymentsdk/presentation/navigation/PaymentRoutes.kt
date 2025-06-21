package com.guavapay.paymentsdk.presentation.navigation

internal sealed interface Route {
  data object HomeRoute : Route
}