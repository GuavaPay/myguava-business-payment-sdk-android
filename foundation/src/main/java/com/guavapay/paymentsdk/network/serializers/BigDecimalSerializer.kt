package com.guavapay.paymentsdk.network.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind.DOUBLE
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal

internal object BigDecimalSerializer : KSerializer<BigDecimal> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigDecimal", DOUBLE)
  override fun serialize(encoder: Encoder, value: BigDecimal) = encoder.encodeDouble(value.toDouble())
  override fun deserialize(decoder: Decoder): BigDecimal = BigDecimal.valueOf(decoder.decodeDouble())
}