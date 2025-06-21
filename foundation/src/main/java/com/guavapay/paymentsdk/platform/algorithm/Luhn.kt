package com.guavapay.paymentsdk.platform.algorithm

internal fun luhn(number: String): Boolean {
  var checksum = 0

  for (i in number.length - 1 downTo 0 step 2) {
    checksum += number[i] - '0'
  }

  for (i in number.length - 2 downTo 0 step 2) {
    val n = (number[i] - '0') * 2
    checksum += if (n > 9) n - 9 else n
  }

  return checksum % 10 == 0
}