package com.guavapay.paymentsdk.network.serializers

import com.guavapay.paymentsdk.gateway.banking.PaymentCardNetworks
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object PaymentCardNetworksSerializer : KSerializer<PaymentCardNetworks> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("PaymentCardNetworks", STRING)
  override fun serialize(encoder: Encoder, value: PaymentCardNetworks) = encoder.encodeString(value.name)
  override fun deserialize(decoder: Decoder) = enumValueOf<PaymentCardNetworks>(decoder.decodeString())
}