package com.kazemieh.network.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

/**
 * Accepts both the WordPress numeric HTTP status and the transitional
 * Spring string status without discarding the rest of the error envelope.
 */
object ApiErrorStatusSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ApiErrorStatus", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String? {
        if (decoder is JsonDecoder) {
            val value = decoder.decodeJsonElement()
            return (value as? JsonPrimitive)?.content
        }
        return decoder.decodeString()
    }

    override fun serialize(encoder: Encoder, value: String?) {
        if (value == null) encoder.encodeNull() else encoder.encodeString(value)
    }
}

@Serializable
data class ApiError(
    val message: String,
    @Serializable(with = ApiErrorStatusSerializer::class)
    val status: String? = null,
    val code: Int? = null,
    val errorCode: String = "UNKNOWN_ERROR",
    val path: String? = null,
    val timestamp: String? = null
)

class ApiException(
    val messageText: Any,
    val code: Int?
) : Exception(if (messageText is String) messageText else "API Error")
