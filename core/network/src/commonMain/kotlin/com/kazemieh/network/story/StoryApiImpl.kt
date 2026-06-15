package com.kazemieh.network.story

import com.kazemieh.network.story.dto.StoryResponse
import com.kazemieh.network.common.safeApiCallRaw
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

class StoryApiImpl(
    private val client: HttpClient
) : StoryApi {

    override suspend fun getStories(): List<StoryResponse> = safeApiCallRaw {
        client.get("api/stories")
    }

    override suspend fun getAdminStories(): List<StoryResponse> = safeApiCallRaw {
        client.get("api/admin/stories")
    }

    override suspend fun createStory(
        bytes: ByteArray,
        mediaType: String,
        productId: Long?,
        title: String?
    ): StoryResponse = safeApiCallRaw {
        client.post("api/admin/stories") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("file", bytes, Headers.build {
                        val extension = if (mediaType == "VIDEO") "mp4" else "jpg"
                        val mimeType = if (mediaType == "VIDEO") "video/mp4" else "image/jpeg"
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"story.$extension\"")
                    })
                    append("mediaType", mediaType)
                    productId?.let { append("productId", it.toString()) }
                    title?.let { append("title", it) }
                }
            ))
        }
    }

    override suspend fun updateStory(
        id: Long,
        productId: Long?,
        title: String?
    ): StoryResponse = safeApiCallRaw {
        client.patch("api/admin/stories/$id") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "productId" to productId,
                "title" to title
            ))
        }
    }

    override suspend fun deleteStory(id: Long) = safeApiCallRaw<Unit> {
        client.delete("api/admin/stories/$id")
    }
}
