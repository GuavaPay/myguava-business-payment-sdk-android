@file:Suppress("FunctionName")

package com.guavapay.paymentsdk.integrations.local

import com.guavapay.paymentsdk.LibraryUnit
import com.guavapay.paymentsdk.integrations.RunLocalIntegration
import kotlinx.serialization.Serializable
import java.io.BufferedReader

@Serializable internal data class Country(val countryCode: String, val countryName: String, val phoneCode: String)

internal suspend fun LocalCountries(lib: LibraryUnit) =
  RunLocalIntegration(lib) {
    lib.network.json.unspecified.decodeFromString<List<Country>>(
      lib.context.assets.open("countries.json").bufferedReader().use(BufferedReader::readText)
    )
  }