package com.guavapay.paymentsdk.network.serializers

import com.guavapay.paymentsdk.gateway.banking.PaymentCardCategory
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object PaymentCardCategorySerializer : KSerializer<PaymentCardCategory> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("PaymentCardCategory", STRING)
  override fun serialize(encoder: Encoder, value: PaymentCardCategory) = encoder.encodeString(value.name)
  override fun deserialize(decoder: Decoder) = enumValueOf<PaymentCardCategory>(decoder.decodeString())
}