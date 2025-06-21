package com.guavapay.paymentsdk.network.serializers

import com.guavapay.paymentsdk.gateway.banking.PaymentCardType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object PaymentCardTypeSerializer : KSerializer<PaymentCardType> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("PaymentCardType", STRING)
  override fun serialize(encoder: Encoder, value: PaymentCardType) = encoder.encodeString(value.name)
  override fun deserialize(decoder: Decoder) = enumValueOf<PaymentCardType>(decoder.decodeString())
}