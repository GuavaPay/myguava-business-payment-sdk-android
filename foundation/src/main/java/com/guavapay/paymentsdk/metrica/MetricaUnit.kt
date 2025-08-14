@file:Suppress("UnstableApiUsage")

package com.guavapay.paymentsdk.metrica

import android.content.Context
import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.gateway.banking.GatewayException
import com.guavapay.paymentsdk.integrations.IntegrationException
import com.guavapay.paymentsdk.logging.SentryLogger
import com.guavapay.paymentsdk.logging.e
import com.guavapay.paymentsdk.logging.i
import com.guavapay.paymentsdk.network.ssevents.SseException
import io.sentry.Breadcrumb
import io.sentry.EventProcessor
import io.sentry.IScope
import io.sentry.MainEventProcessor
import io.sentry.Scope
import io.sentry.Scopes
import io.sentry.SentryClient
import io.sentry.SentryLevel
import io.sentry.UncaughtExceptionHandlerIntegration
import io.sentry.android.core.ActivityFramesTracker
import io.sentry.android.core.AnrV2EventProcessor
import io.sentry.android.core.BuildInfoProvider
import io.sentry.android.core.SentryAndroidOptions
import io.sentry.protocol.User
import io.sentry.util.LoadClass

internal class MetricaUnit(private val lib: LibraryUnit) {
  private val options = SentryAndroidOptions().apply {
    dsn = "https://aa8917c4ee21e2da40b94e26f0db755b@o4507129772310528.ingest.de.sentry.io/4509802634215505"
    environment = lib.state.payload?.circuit?.name

    tracesSampleRate = 0.10
    profilesSampleRate = 0.10

    isEnableUserInteractionTracing = true
    isEnableUserInteractionBreadcrumbs = true
    isAttachThreads = true

    proguardUuid = "7FC967FA-07F0-48E6-A3B7-EE86703DB9E6"
    release = "sdk@0.5.2"
    dist = "0.5.2.public.release"

    isCollectAdditionalContext = true
    isAnrEnabled = true
    isEnablePerformanceV2 = true
    isSendDefaultPii = true

    isEnableUncaughtExceptionHandler = true

    setBeforeSend { event, _ ->
      if (isSdkRelatedException(event.throwable)) {
        i("SDK related exception captured")
        return@setBeforeSend event
      }
      return@setBeforeSend null
    }

    addEventProcessor(AnrV2EventProcessor(lib.context, this, BuildInfoProvider(SentryLogger())))
    addEventProcessor(MainEventProcessor(this))
    createDefaultAndroidEventProcessor(lib.context, this)?.let(::addEventProcessor)
    createSentryRuntimeEventProcessor()?.let(::addEventProcessor)
    createPerformanceAndroidEventProcessor(this)?.let(::addEventProcessor)

    i("Metrica initialized")
  }

  private val globalScope = Scope(options)
  private val isolationScope = Scope(options)
  private val defaultScope = Scope(options)

  private val client = globalScope.bindClient(SentryClient(options))
  private val scopes = Scopes(defaultScope, isolationScope, globalScope, "MetricaUnit::init")

  init {
    UncaughtExceptionHandlerIntegration().also(options::addIntegration).register(scopes, options)
  }

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

  fun event(
    message: String,
    level: SentryLevel = SentryLevel.INFO,
    tags: Map<String, String> = emptyMap(),
    extras: Map<String, String> = emptyMap(),
    contexts: Map<String, Any> = emptyMap()
  ) {
    scopes.captureMessage(message, level) { scope ->
      scope.fillContexts()
      tags.forEach { (k, v) -> scope.setTag(k, v) }
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

  private fun isSdkRelatedException(throwable: Throwable?): Boolean {
    if (throwable == null) return false
    if (isDirectSdkException(throwable)) return true
    if (hasStackTraceInSdkPackages(throwable)) return true

    val cause = throwable.cause
    if (cause != null && isSdkRelatedException(cause)) return true

    throwable.suppressed.forEach { suppressed ->
      if (isSdkRelatedException(suppressed)) return true
    }

    return false
  }

  private fun isDirectSdkException(throwable: Throwable): Boolean {
    val className = throwable.javaClass.name

    return when {
      throwable is GatewayException -> true
      throwable is IntegrationException -> true
      throwable is SseException -> true
      className.startsWith("com.guavapay.paymentsdk.") -> true
      else -> false
    }
  }

  private fun hasStackTraceInSdkPackages(throwable: Throwable) =
    throwable.stackTrace.any { stackTraceElement ->
      val className = stackTraceElement.className
      className.startsWith("com.guavapay.paymentsdk.") || className.startsWith("com.guavapay.myguava.business.myguava3ds2.")
    }

  private fun createDefaultAndroidEventProcessor(context: Context, options: SentryAndroidOptions) = try {
    val className = "io.sentry.android.core.DefaultAndroidEventProcessor"
    val clazz = Class.forName(className)
    val buildInfoProvider = BuildInfoProvider(SentryLogger())
    val constructor = clazz.getDeclaredConstructor(Context::class.java, BuildInfoProvider::class.java, SentryAndroidOptions::class.java)
    constructor.isAccessible = true
    (constructor.newInstance(context, buildInfoProvider, options) as EventProcessor).also {
      i("Successfully created DefaultAndroidEventProcessor via reflection")
    }
  } catch (e: Exception) {
    e("Failed to create DefaultAndroidEventProcessor via reflection", e)
    null
  }

  private fun createSentryRuntimeEventProcessor(): EventProcessor? = try {
    val className = "io.sentry.SentryRuntimeEventProcessor"
    val clazz = Class.forName(className)
    val constructor = clazz.getDeclaredConstructor()
    constructor.isAccessible = true
    (constructor.newInstance() as EventProcessor).also {
      i("Successfully created SentryRuntimeEventProcessor via reflection")
    }
  } catch (e: Exception) {
    e("Failed to create SentryRuntimeEventProcessor via reflection", e)
    null
  }

  private fun createPerformanceAndroidEventProcessor(options: SentryAndroidOptions): EventProcessor? = try {
    val className = "io.sentry.android.core.PerformanceAndroidEventProcessor"
    val clazz = Class.forName(className)
    val constructor = clazz.getDeclaredConstructor(SentryAndroidOptions::class.java, ActivityFramesTracker::class.java)
    constructor.isAccessible = true
    (constructor.newInstance(options, ActivityFramesTracker(LoadClass(), options)) as EventProcessor).also {
      i("Successfully created PerformanceAndroidEventProcessor via reflection")
    }
  } catch (e: Exception) {
    e("Failed to create PerformanceAndroidEventProcessor via reflection", e)
    null
  }
}
