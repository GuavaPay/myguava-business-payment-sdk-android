package com.guavapay.paymentsdk.network.serializers

import com.guavapay.paymentsdk.gateway.banking.PaymentCardScheme
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object PaymentCardSchemeSerializer : KSerializer<PaymentCardScheme> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("PaymentCardScheme", STRING)
  override fun serialize(encoder: Encoder, value: PaymentCardScheme) = encoder.encodeString(value.name)
  override fun deserialize(decoder: Decoder) = enumValueOf<PaymentCardScheme>(decoder.decodeString())
}