package com.guavapay.paymentsdk.platform.manifest

import android.content.Context
import android.content.pm.PackageManager.GET_META_DATA

internal data class ManifestFields(val baseUrl: String)

internal fun Context.manifestFields() = runCatching {
  val ai = packageManager.getApplicationInfo(packageName, GET_META_DATA)
  val metaData = ai.metaData
  val environment = metaData?.getString(META_DATA_ENV) ?: DEFAULT_ENV
  val baseUrl = env2url(environment)
  ManifestFields(baseUrl = baseUrl)
}.getOrElse { ManifestFields(baseUrl = env2url(DEFAULT_ENV)) }

private const val META_DATA_ENV = "com.guavapay.paymentsdk.environment"
private const val DEFAULT_ENV = "prod"

private fun env2url(env: String) = when (env.lowercase()) {
  "dev" -> "https://cardium-cpg-dev.guavapay.com"
  "sandbox" -> "https://sandbox-pgw.myguava.com"
  "prod" -> "https://api-pgw.myguava.com"
  else -> "https://api-pgw.myguava.com"
}