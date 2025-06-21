package com.guavapay.paymentsdk.presentation.platform

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CardNumberVisualTransformation : VisualTransformation {
  override fun filter(text: AnnotatedString): TransformedText {
    val trimmed = if (text.text.length >= 19) text.text.substring(0..18) else text.text
    val filtered = trimmed.filter { it.isDigit() }

    var out = ""
    for (i in filtered.indices) {
      if (i > 0 && i % 4 == 0) out += " "
      out += filtered[i]
    }

    return TransformedText(AnnotatedString(out), cardNumberOffsetTranslator)
  }

  private val cardNumberOffsetTranslator = object : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
      if (offset <= 0) return 0
      if (offset > 19) return 23 // 19 digits + 4 spaces

      val spaces = (offset - 1) / 4
      return offset + spaces
    }

    override fun transformedToOriginal(offset: Int): Int {
      if (offset <= 0) return 0

      var originalOffset = 0
      var transformedCount = 0

      while (transformedCount < offset && originalOffset < 19) {
        if (originalOffset > 0 && originalOffset % 4 == 0) {
          transformedCount++
          if (transformedCount >= offset) break
        }
        originalOffset++
        transformedCount++
      }

      return originalOffset
    }
  }
}

class CvcVisualTransformation : VisualTransformation {
  override fun filter(text: AnnotatedString): TransformedText {
    val trimmed = if (text.text.length >= 4) text.text.substring(0..3) else text.text
    val filtered = trimmed.filter { it.isDigit() }
    val masked = "*".repeat(filtered.length)
    return TransformedText(AnnotatedString(masked), cvcOffsetTranslator)
  }

  private val cvcOffsetTranslator = object : OffsetMapping {
    override fun originalToTransformed(offset: Int) = offset
    override fun transformedToOriginal(offset: Int) = offset
  }
}

class ExpiryDateVisualTransformation : VisualTransformation {
  override fun filter(text: AnnotatedString): TransformedText {
    val trimmed = if (text.text.length >= 4) text.text.substring(0..3) else text.text
    val filtered = trimmed.filter { it.isDigit() }

    var out = ""
    for (i in filtered.indices) {
      if (i == 2) out += "/"
      out += filtered[i]
    }

    return TransformedText(AnnotatedString(out), expiryDateOffsetTranslator)
  }

  private val expiryDateOffsetTranslator = object : OffsetMapping {
    override fun originalToTransformed(offset: Int) =
      when (offset) {
        0, 1, 2 -> offset
        3, 4 -> offset + 1 // Add 1 for the slash
        else -> 5 // Max length is MM/YY = 5 characters
      }

    override fun transformedToOriginal(offset: Int) =
      when (offset) {
        0, 1, 2 -> offset
        3 -> 2 // Slash position maps to position 2
        4, 5 -> offset - 1 // Subtract 1 for the slash
        else -> 4 // Max original length is 4 digits
      }
  }
}