package com.kazemieh.data.bundle.repository

import com.kazemieh.common.AppResult
import com.kazemieh.domain.bundle.AdminBundle
import com.kazemieh.domain.bundle.AdminBundleParams
import com.kazemieh.domain.bundle.AdminBundleRepository
import com.kazemieh.domain.bundle.AdminBundleUpdateParams
import com.kazemieh.network.bundle.AdminBundleApi
import com.kazemieh.network.bundle.dto.AdminBundleResponse
import com.kazemieh.network.bundle.dto.AdminCreateBundleRequestDto
import com.kazemieh.network.bundle.dto.AdminUpdateBundleRequestDto
import com.kazemieh.network.common.safeApiCall

class AdminBundleRepositoryImpl(
    private val api: AdminBundleApi
) : AdminBundleRepository {

    override suspend fun listBundles(): AppResult<List<AdminBundle>> = safeApiCall {
        api.listBundles().map { it.toDomain() }
    }

    override suspend fun createBundle(params: AdminBundleParams): AppResult<Long> = safeApiCall {
        api.createBundle(
            AdminCreateBundleRequestDto(
                title = params.title, slug = params.slug, description = params.description,
                productId = params.productId, memberProductIds = params.memberProductIds, isActive = params.isActive
            )
        )
    }

    override suspend fun updateBundle(id: Long, params: AdminBundleUpdateParams): AppResult<Unit> = safeApiCall {
        api.updateBundle(
            id,
            AdminUpdateBundleRequestDto(
                title = params.title, description = params.description,
                memberProductIds = params.memberProductIds, isActive = params.isActive
            )
        )
    }

    override suspend fun deleteBundle(id: Long): AppResult<Unit> = safeApiCall {
        api.deleteBundle(id)
    }

    private fun AdminBundleResponse.toDomain() = AdminBundle(
        id = id, title = title, slug = slug, description = description,
        productId = productId, memberProductIds = memberProductIds, isActive = isActive
    )
}
