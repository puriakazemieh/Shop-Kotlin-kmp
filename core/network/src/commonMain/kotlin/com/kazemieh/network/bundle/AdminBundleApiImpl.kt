package com.kazemieh.network.bundle

import com.kazemieh.network.bundle.dto.AdminBundleResponse
import com.kazemieh.network.bundle.dto.AdminCreateBundleRequestDto
import com.kazemieh.network.bundle.dto.AdminUpdateBundleRequestDto
import com.kazemieh.network.bundle.dto.IdResponse
import com.kazemieh.network.common.safeApiCallRaw
import io.ktor.client.*
import io.ktor.client.request.*

class AdminBundleApiImpl(
    private val client: HttpClient
) : AdminBundleApi {

    override suspend fun listBundles(): List<AdminBundleResponse> = safeApiCallRaw {
        client.get("api/admin/bundles")
    }

    override suspend fun createBundle(request: AdminCreateBundleRequestDto): Long =
        safeApiCallRaw<IdResponse> {
            client.post("api/admin/bundles") { setBody(request) }
        }.id

    override suspend fun updateBundle(id: Long, request: AdminUpdateBundleRequestDto): Unit = safeApiCallRaw {
        client.patch("api/admin/bundles/$id") { setBody(request) }
    }

    override suspend fun deleteBundle(id: Long): Unit = safeApiCallRaw {
        client.delete("api/admin/bundles/$id")
    }
}
