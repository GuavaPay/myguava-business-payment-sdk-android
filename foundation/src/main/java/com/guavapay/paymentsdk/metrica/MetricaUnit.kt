@file:Suppress("UnstableApiUsage")

package com.guavapay.paymentsdk.metrica

import com.guavapay.paymentsdk.LibraryUnit
import io.sentry.Breadcrumb
import io.sentry.IScope
import io.sentry.Scope
import io.sentry.Scopes
import io.sentry.SentryClient
import io.sentry.SentryLevel
import io.sentry.SentryOptions
import io.sentry.protocol.User

internal class MetricaUnit(private val lib: LibraryUnit) {
  private val options = SentryOptions().apply {
    dsn = "https://aa8917c4ee21e2da40b94e26f0db755b@o4507129772310528.ingest.de.sentry.io/4509802634215505"
    environment = lib.state.payload?.circuit?.name

    tracesSampleRate = 0.10
    profilesSampleRate = 0.10

    isEnableUserInteractionTracing = true
    isEnableUserInteractionBreadcrumbs = true
    isAttachThreads = true

    release = "sdk@0.5.2"
    dist = "0.5.2.public.release"
  }

  private val globalScope = Scope(options)
  private val isolationScope = Scope(options)
  private val defaultScope = Scope(options)

  private val client = globalScope.bindClient(SentryClient(options))
  private val scopes = Scopes(defaultScope, isolationScope, globalScope, "MetricaUnit::init")

  fun exception(throwable: Throwable) {
    scopes.withScope { scope ->
      scope.fillContexts()
      scopes.captureException(throwable)
    }
  }

  fun breadcrumb(message: String, category: String = "unspecified", type: String = "unspecified", level: SentryLevel = SentryLevel.INFO, data: Map<String, Any?> = emptyMap()) {
    scopes.addBreadcrumb(
      Breadcrumb().apply {
        this.message = message
        this.level = level
        this.category = category
        this.type = type
        this.data.putAll(data)
      }
    )
  }

  fun event(message: String, level: SentryLevel = SentryLevel.INFO, tags: Map<String, String> = emptyMap(), extras: Map<String, String> = emptyMap(), contexts: Map<String, Any> = emptyMap()) {
    scopes.captureMessage(message, level) { scope ->
      scope.fillContexts()
      tags.forEach  { (k, v) -> scope.setTag(k, v) }
      extras.forEach { (k, v) -> scope.setExtra(k, v) }
      contexts.forEach { (k, v) -> scope.setContexts(k, v) }
    }
  }

  private fun IScope.fillContexts() {
    val payload = lib.state.payload
    payload?.run {
      setContexts("order", mapOf("order_id" to orderId))
      locale?.let { setContexts("locale", mapOf("locale" to it)) }
      circuit?.let { setContexts("circuit", mapOf("circuit" to it)) }
      setContexts("schemes", mapOf("schemes" to schemes))
      setContexts("methods", mapOf("methods" to methods))
      setContexts("categories", mapOf("categories" to categories))
    }
    lib.state.device.ip?.let { user = User().apply { ipAddress = it } }
  }

  fun close() = scopes.close()
}
