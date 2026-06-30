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
        title: String?,
        linkType: String,
        categoryId: Long?,
        blogSlug: String?
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
                    append("linkType", linkType)
                    productId?.let { append("productId", it.toString()) }
                    categoryId?.let { append("categoryId", it.toString()) }
                    blogSlug?.let { append("blogSlug", it) }
                    title?.let { append("title", it) }
                }
            ))
        }
    }

    override suspend fun updateStory(
        id: Long,
        productId: Long?,
        title: String?,
        linkType: String?,
        categoryId: Long?,
        blogSlug: String?
    ): StoryResponse = safeApiCallRaw {
        client.patch("api/admin/stories/$id") {
            contentType(ContentType.Application.Json)
            setBody(buildMap<String, Any?> {
                put("productId", productId)
                put("title", title)
                linkType?.let { put("linkType", it) }
                put("categoryId", categoryId)
                put("blogSlug", blogSlug)
            })
        }
    }

    override suspend fun deleteStory(id: Long) = safeApiCallRaw<Unit> {
        client.delete("api/admin/stories/$id")
    }
}
