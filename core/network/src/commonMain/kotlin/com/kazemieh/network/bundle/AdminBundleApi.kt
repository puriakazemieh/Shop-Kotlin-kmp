package com.kazemieh.network.bundle

import com.kazemieh.network.bundle.dto.AdminBundleResponse
import com.kazemieh.network.bundle.dto.AdminCreateBundleRequestDto
import com.kazemieh.network.bundle.dto.AdminUpdateBundleRequestDto

interface AdminBundleApi {
    suspend fun listBundles(): List<AdminBundleResponse>
    suspend fun createBundle(request: AdminCreateBundleRequestDto): Long
    suspend fun updateBundle(id: Long, request: AdminUpdateBundleRequestDto)
    suspend fun deleteBundle(id: Long)
}
