@file:Suppress("UnstableApiUsage")

package com.guavapay.paymentsdk.metrica

import android.app.Application
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
import io.sentry.Hint
import io.sentry.MainEventProcessor
import io.sentry.Scope
import io.sentry.Scopes
import io.sentry.SentryClient
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.UncaughtExceptionHandlerIntegration
import io.sentry.android.core.ActivityBreadcrumbsIntegration
import io.sentry.android.core.ActivityFramesTracker
import io.sentry.android.core.ActivityLifecycleIntegration
import io.sentry.android.core.AnrIntegrationFactory
import io.sentry.android.core.AnrV2EventProcessor
import io.sentry.android.core.AppComponentsBreadcrumbsIntegration
import io.sentry.android.core.BuildInfoProvider
import io.sentry.android.core.NetworkBreadcrumbsIntegration
import io.sentry.android.core.SentryAndroidOptions
import io.sentry.android.core.SystemEventsBreadcrumbsIntegration
import io.sentry.android.core.UserInteractionIntegration
import io.sentry.protocol.User
import io.sentry.util.LoadClass
import kotlinx.coroutines.CancellationException

internal class MetricaUnit(private val lib: LibraryUnit) {
  private val options = SentryAndroidOptions().apply {
    dsn = "https://aa8917c4ee21e2da40b94e26f0db755b@o4507129772310528.ingest.de.sentry.io/4509802634215505"
    environment = lib.state.payload?.environment?.name

    tracesSampleRate = 1.0
    profilesSampleRate = 0.5

    isEnableUserInteractionTracing = true
    isEnableUserInteractionBreadcrumbs = true
    isAttachThreads = true

    proguardUuid = "CCE0349D-B7A2-4A90-B688-87FEAA87F10D"
    addBundleId("CCE0349D-B7A2-4A90-B688-87FEAA87F10D")
    release = "sdk@0.6.0"
    dist = "0.6.0.public.release"

    isCollectAdditionalContext = true
    isAnrEnabled = true
    isEnablePerformanceV2 = true
    isSendDefaultPii = true
    isEnableAppStartProfiling = true

    isEnableUncaughtExceptionHandler = true
    flushTimeoutMillis = 1000

    addIgnoredExceptionForType(CancellationException::class.java)

    setBeforeSend { event, _ ->
      i("Sending event to Sentry: ${event.eventId}")

      if (event.throwable == null) {
        i("Non-exception event captured")
        return@setBeforeSend event
      }

      if (isSdkRelatedException(event.throwable)) {
        i("SDK related exception captured")
        return@setBeforeSend event
      }
      return@setBeforeSend null
    }

    val logger = SentryLogger()

    setLogger(logger)

    val lc = LoadClass()
    val bip = BuildInfoProvider(SentryLogger())
    addEventProcessor(AnrV2EventProcessor(lib.context, this, bip))
    addEventProcessor(MainEventProcessor(this))
    createDefaultAndroidEventProcessor(lib.context, this)?.let(::addEventProcessor)
    createSentryRuntimeEventProcessor()?.let(::addEventProcessor)
    createPerformanceAndroidEventProcessor(this)?.let(::addEventProcessor)
    addEventProcessor(PayloadEventProcessor(lib))

    integrations.add(UncaughtExceptionHandlerIntegration())
    integrations.add(AnrIntegrationFactory.create(lib.context, bip))

    val app = lib.context.applicationContext as Application
    addIntegration(ActivityLifecycleIntegration(app, bip, ActivityFramesTracker(lc, this)))
    addIntegration(ActivityBreadcrumbsIntegration(app))
    addIntegration(UserInteractionIntegration(app, lc))
    addIntegration(AppComponentsBreadcrumbsIntegration(lib.context))
    addIntegration(SystemEventsBreadcrumbsIntegration(lib.context))
    addIntegration(NetworkBreadcrumbsIntegration(lib.context, bip, logger))

    i("Metrica initialized")
  }

  private val globalScope = Scope(options)
  private val isolationScope = Scope(options)
  private val defaultScope = Scope(options)

  private val client = globalScope.bindClient(SentryClient(options))
  private val scopes = Scopes(defaultScope, isolationScope, globalScope, "MetricaUnit::init")

  fun exception(throwable: Throwable) {
    scopes.options.environment = lib.state.payload?.environment?.name?.lowercase()
    scopes.withScope {
      scopes.captureException(throwable)
    }
  }

  fun breadcrumb(message: String, category: String = "unspecified", type: String = "unspecified", level: SentryLevel = SentryLevel.INFO, data: Map<String, Any?> = emptyMap()) {
    scopes.options.environment = lib.state.payload?.environment?.name?.lowercase()

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
    scopes.options.environment = lib.state.payload?.environment?.name?.lowercase()

    scopes.captureMessage(message, level) { scope ->
      tags.forEach { (k, v) -> scope.setTag(k, v) }
      extras.forEach { (k, v) -> scope.setExtra(k, v) }
      contexts.forEach { (k, v) -> scope.setContexts(k, v) }
    }
  }

  fun close() = scopes.close()

  private fun isSdkRelatedException(throwable: Throwable?): Boolean {
    if (throwable == null) return false
    if (throwable is CancellationException) return false
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
      className.startsWith("com.guavapay.paymentsdk.") || className.startsWith("com.guavapay.myguava.business.myguava3ds2.") || className.startsWith("kotlinx.coroutines.TimeoutKt")
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

  class PayloadEventProcessor(private val lib: LibraryUnit) : EventProcessor {
    override fun process(event: SentryEvent, hint: Hint): SentryEvent {
      val p = lib.state.payload ?: return event
      event.setExtra("Order ID", p.orderId)
      p.locale?.let { event.setExtra("Locale", it.toString()) }
      event.contexts["Payment Details"] = mapOf(
        "Card Schemes" to p.availableCardSchemes,
        "Payment Methods" to p.availablePaymentMethods,
        "Card Product Categories" to p.availableCardProductCategories,
        "Payment Method" to lib.state.analytics.paymentMethod,
        "Merchant Name" to lib.state.analytics.merchantName,
      )

      event.contexts["Request"] = mapOf("ID" to lib.state.analytics.requestId)
      event.setExtra("Card Schemes", p.availableCardSchemes.joinToString(","))
      event.setExtra("Payment Methods", p.availablePaymentMethods.joinToString(","))
      event.setExtra("Card Product Categories", p.availableCardProductCategories.joinToString(","))
      event.setExtra("Payment Method", lib.state.analytics.paymentMethod)
      event.setExtra("Merchant Name", lib.state.analytics.merchantName)
      event.setExtra("Request ID", lib.state.analytics.requestId)

      p.environment?.name?.lowercase()?.let { event.environment = it }
      lib.state.device.ip?.let { ip ->
        val u = event.user ?: User()
        u.ipAddress = ip
        event.user = u
      }
      return event
    }
  }
}
