package com.guavapay.paymentsdk.network.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Currency

internal object CurrencySerializer : KSerializer<Currency> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("java.util.Currency", STRING)
  override fun serialize(encoder: Encoder, value: Currency) = encoder.encodeString(value.currencyCode)
  override fun deserialize(decoder: Decoder): Currency = Currency.getInstance(decoder.decodeString())
}