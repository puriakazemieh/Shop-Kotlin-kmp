package com.kazemieh.network.blog

import com.kazemieh.network.blog.dto.request.*
import com.kazemieh.network.blog.dto.response.*
import com.kazemieh.network.common.safeApiCallRaw
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.core.*

class BlogApiImpl(
    private val httpClient: HttpClient
) : BlogApi {
    override suspend fun getBlogs(
        page: Int,
        size: Int,
        categoryId: Long?,
        searchQuery: String?
    ): BlogListResponse {
        return safeApiCallRaw {
            httpClient.get("api/blogs") {
                parameter("page", page)
                parameter("size", size)
                categoryId?.let { parameter("categoryId", it) }
                searchQuery?.let { parameter("search", it) }
            }
        }
    }

    override suspend fun getBlogDetail(slug: String): BlogResponse {
        return safeApiCallRaw {
            httpClient.get("api/blogs/$slug")
        }
    }

    override suspend fun getRelatedBlogs(slug: String): List<BlogResponse> {
        return safeApiCallRaw {
            httpClient.get("api/blogs/$slug/related")
        }
    }

    override suspend fun getCategories(): List<BlogCategoryResponse> {
        return safeApiCallRaw {
            httpClient.get("api/blogs/categories")
        }
    }

    override suspend fun getAdminBlogs(page: Int, size: Int): BlogListResponse {
        return safeApiCallRaw {
            httpClient.get("api/admin/blogs") {
                parameter("page", page)
                parameter("size", size)
            }
        }
    }

    override suspend fun createBlog(request: CreateBlogRequest): BlogResponse {
        return safeApiCallRaw {
            httpClient.post("api/admin/blogs") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun updateBlog(id: Long, request: UpdateBlogRequest): BlogResponse {
        return safeApiCallRaw {
            httpClient.put("api/admin/blogs/$id") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun deleteBlog(id: Long) {
        return safeApiCallRaw {
            httpClient.delete("api/admin/blogs/$id")
        }
    }

    override suspend fun uploadMedia(fileBytes: ByteArray, fileName: String): UploadMediaResponse {
        return safeApiCallRaw {
            httpClient.submitFormWithBinaryData(
                url = "api/admin/blogs/media/upload",
                formData = formData {
                    append("file", fileBytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/png")
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                }
            )
        }
    }

    override suspend fun createCategory(request: CreateCategoryRequest): BlogCategoryResponse {
        return safeApiCallRaw {
            httpClient.post("api/admin/blogs/categories") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun updateCategory(id: Long, request: UpdateCategoryRequest): BlogCategoryResponse {
        return safeApiCallRaw {
            httpClient.put("api/admin/blogs/categories/$id") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    override suspend fun deleteCategory(id: Long) {
        return safeApiCallRaw {
            httpClient.delete("api/admin/blogs/categories/$id")
        }
    }
}
