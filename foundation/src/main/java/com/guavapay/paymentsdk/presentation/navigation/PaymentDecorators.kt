@file:Suppress("FunctionName")

package com.guavapay.paymentsdk.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.navEntryDecorator
import androidx.navigation3.ui.LocalEntriesToRenderInCurrentScene
import com.guavapay.paymentsdk.logging.d
import com.guavapay.paymentsdk.rememberLibraryUnit

@Composable internal inline fun rememberLoggingNavEntryDecorator(
  noinline nameOf: (Any) -> String = { it::class.simpleName ?: it.toString() },
  noinline onEnter: (String) -> Unit = { d(it) },
  noinline onExit: (String) -> Unit = { d(it) },
): NavEntryDecorator<Any> = remember { LoggingNavEntryDecorator(nameOf, onEnter, onExit) }

internal fun LoggingNavEntryDecorator(
  nameOf: (Any) -> String = { it::class.simpleName ?: it.toString() },
  onEnter: (String) -> Unit = { d(it) },
  onExit: (String) -> Unit = { d(it) }
): NavEntryDecorator<Any> = navEntryDecorator { entry ->
  val key = entry.key
  if (LocalEntriesToRenderInCurrentScene.current.contains(key)) {
    key(key) {
      LaunchedEffect(key) {
        onEnter("Navigation action executed: → ${nameOf(key)}")
      }
      DisposableEffect(key) {
        onDispose { onExit("Navigation action executed: ← ${nameOf(key)}") }
      }
      entry.content(key)
    }
  }
}

@Composable internal fun rememberMetricaNavEntryDecorator(): NavEntryDecorator<Any> {
  val lib = rememberLibraryUnit()
  return navEntryDecorator { entry ->
    val key = entry.key
    if (LocalEntriesToRenderInCurrentScene.current.contains(key)) {
      key(key) {
        LaunchedEffect(key) {
          lib.metrica.breadcrumb(
            message = "Navigation: -> ${key::class.simpleName}",
            category = "navigation",
            type = "action"
          )
        }
        DisposableEffect(key) {
          onDispose {
            lib.metrica.breadcrumb(
              message = "Navigation: <- ${key::class.simpleName}",
              category = "navigation",
              type = "action",
            )
          }
        }
        entry.content(key)
      }
    }
  }
}