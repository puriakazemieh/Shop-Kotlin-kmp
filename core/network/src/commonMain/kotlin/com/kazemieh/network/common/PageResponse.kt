package com.kazemieh.network.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.builtins.ListSerializer

@Serializable(with = PageResponseSerializer::class)
data class PageResponse<T>(
    val items: List<T>,
    val page: PageMetadata
)

@Serializable
data class PageMetadata(
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    @OptIn(ExperimentalSerializationApi::class)
    @JsonNames("number", "page")
    val number: Int
)

class PageResponseSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<PageResponse<T>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PageResponse")

    override fun deserialize(decoder: Decoder): PageResponse<T> {
        val input = decoder as? JsonDecoder ?: throw SerializationException("This class can be decoded only by Json")
        val jsonObject = input.decodeJsonElement().jsonObject

        val items = jsonObject["items"]?.jsonArray?.map { input.json.decodeFromJsonElement(dataSerializer, it) }
            ?: jsonObject["content"]?.jsonArray?.map { input.json.decodeFromJsonElement(dataSerializer, it) }
            ?: emptyList()

        val pageElement = jsonObject["page"]
        val metadata = if (pageElement is JsonObject) {
            input.json.decodeFromJsonElement<PageMetadata>(pageElement)
        } else {
            val number = pageElement?.jsonPrimitive?.int ?: 0
            val size = jsonObject["size"]?.jsonPrimitive?.int ?: 0
            val totalElements = jsonObject["totalElements"]?.jsonPrimitive?.long ?: 0L
            val totalPages = jsonObject["totalPages"]?.jsonPrimitive?.int ?: 0
            PageMetadata(size, totalElements, totalPages, number)
        }

        return PageResponse(items, metadata)
    }

    override fun serialize(encoder: Encoder, value: PageResponse<T>) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("This class can be encoded only by Json")
        val json = buildJsonObject {
            put("items", output.json.encodeToJsonElement(ListSerializer(dataSerializer), value.items))
            put("page", output.json.encodeToJsonElement(PageMetadata.serializer(), value.page))
        }
        output.encodeJsonElement(json)
    }
}
