package com.guavapay.paymentsdk.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.guavapay.paymentsdk.presentation.navigation.Route
import java.io.Serializable

internal interface Screen<RouteType : Route, Actions : Serializable> : Serializable {
  @Composable operator fun invoke(nav: SnapshotStateList<Route>, route: RouteType, actions: Actions)
}