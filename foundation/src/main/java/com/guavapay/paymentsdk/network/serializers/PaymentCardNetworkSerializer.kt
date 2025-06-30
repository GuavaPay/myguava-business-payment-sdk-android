package com.guavapay.paymentsdk.network.serializers

import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetwork
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object PaymentCardNetworkSerializer : KSerializer<PaymentCardNetwork> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("PaymentCardNetwork", STRING)
  override fun serialize(encoder: Encoder, value: PaymentCardNetwork) = encoder.encodeString(value.name)
  override fun deserialize(decoder: Decoder) = enumValueOf<PaymentCardNetwork>(decoder.decodeString())
}