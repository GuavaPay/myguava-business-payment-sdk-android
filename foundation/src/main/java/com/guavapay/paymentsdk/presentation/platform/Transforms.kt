package com.guavapay.paymentsdk.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.google.i18n.phonenumbers.PhoneNumberUtil

internal class CardNumberVisualTransformation : VisualTransformation {
  override fun filter(text: AnnotatedString): TransformedText {
    val trimmed = if (text.text.length >= 19) text.text.substring(0..18) else text.text
    val filtered = trimmed.filter { it.isDigit() }

    var out = ""
    for (i in filtered.indices) {
      if (i > 0 && i % 4 == 0) out += " "
      out += filtered[i]
    }

    return TransformedText(AnnotatedString(out), offset)
  }

  private val offset = object : OffsetMapping {
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

@Composable internal fun rememberCvcPeekState(maskChar: Char = '●', maxLength: Int = 3) = remember { CvcPeekState(maskChar, maxLength) }

internal class CvcPeekState(private val maskChar: Char, private val maxLength: Int) {
  private var revealIndex by mutableStateOf<Int?>(null)
  private var prevLen by mutableStateOf(0)

  fun onTextChanged(newText: String) {
    val len = newText.count { it in '0'..'9' }
    val isInsertion = len > prevLen

    revealIndex = when {
      len == 0 -> null
      len >= maxLength -> null
      isInsertion -> len - 1
      else -> null
    }

    prevLen = len
  }

  fun onFocusLost() {
    revealIndex = null
  }

  fun visualTransformation() = VisualTransformation { src ->
    val raw = src.text
    if (raw.isEmpty()) {
      return@VisualTransformation TransformedText(AnnotatedString(""), IdentityOffset)
    }

    val len = raw.count { it in '0'..'9' }
    val reveal = if (len >= maxLength) null else revealIndex?.coerceIn(0, raw.lastIndex)

    val masked = buildString(raw.length) {
      raw.forEachIndexed { i, ch ->
        append(if (i == reveal) ch else maskChar)
      }
    }

    TransformedText(AnnotatedString(masked), IdentityOffset)
  }

  private object IdentityOffset : OffsetMapping {
    override fun originalToTransformed(offset: Int) = offset
    override fun transformedToOriginal(offset: Int) = offset
  }
}

internal class ExpiryDateVisualTransformation : VisualTransformation {
  override fun filter(text: AnnotatedString): TransformedText {
    val trimmed = if (text.text.length >= 4) text.text.substring(0..3) else text.text
    val filtered = trimmed.filter { it.isDigit() }

    var out = ""
    for (i in filtered.indices) {
      if (i == 2) out += "/"
      out += filtered[i]
    }

    return TransformedText(AnnotatedString(out), offset)
  }

  private val offset = object : OffsetMapping {
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

internal class PhoneNumberVisualTransformation(private val regionIso: String) : VisualTransformation {
  override fun filter(text: AnnotatedString): TransformedText {
    val src = text.text
    val fmt = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(regionIso)

    val mapping = IntArray(src.length + 1)
    var formatted = ""
    mapping[0] = 0

    for (i in src.indices) {
      val ch = src[i]
      if (ch.isDigit()) formatted = fmt.inputDigit(ch)
      mapping[i + 1] = formatted.length
    }

    val tLen = formatted.length
    val offset = object : OffsetMapping {
      override fun originalToTransformed(offset: Int): Int =
        when {
          offset <= 0 -> 0
          offset >= mapping.size -> tLen
          else -> mapping[offset].coerceIn(0, tLen)
        }

      override fun transformedToOriginal(offset: Int): Int {
        val t = offset.coerceIn(0, tLen)
        var i = 0
        while (i < mapping.size && mapping[i] <= t) i++
        return (i - 1).coerceAtLeast(0)
      }
    }

    return TransformedText(AnnotatedString(formatted), offset)
  }
}