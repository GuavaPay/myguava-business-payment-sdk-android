package com.guavapay.paymentsdk.network.local

import com.guavapay.paymentsdk.logging.e
import java.net.InetAddress
import java.net.NetworkInterface.getNetworkInterfaces

internal fun localipv4() = runCatching {
  val interfaces = getNetworkInterfaces()
  while (interfaces.hasMoreElements()) {
    val networkInterface = interfaces.nextElement()
    if (networkInterface.isLoopback || !networkInterface.isUp) continue

    val addresses = networkInterface.inetAddresses
    while (addresses.hasMoreElements()) {
      val address = addresses.nextElement()

      if (!address.isLoopbackAddress && !address.isLinkLocalAddress && address is InetAddress && address.hostAddress?.contains(':') == false) {
        return@runCatching address.hostAddress
      }
    }
  }

  return@runCatching null
}.onFailure { e("Failed to get local ipv4 address: $it") }.getOrNull()