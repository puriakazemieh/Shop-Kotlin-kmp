package com.kazemieh.data.bundle.repository

import com.kazemieh.common.AppResult
import com.kazemieh.data.catalog.mapper.toCatalogDomain
import com.kazemieh.domain.bundle.BundleDetail
import com.kazemieh.domain.bundle.BundleRepository
import com.kazemieh.domain.bundle.BundleSummary
import com.kazemieh.network.bundle.BundleApi
import com.kazemieh.network.bundle.dto.BundleDetailResponse
import com.kazemieh.network.bundle.dto.BundleSummaryResponse
import com.kazemieh.network.common.safeApiCall
import com.kazemieh.config.capabilities.AssetUrlResolver

class BundleRepositoryImpl(
    private val api: BundleApi,
    private val assetUrlResolver: AssetUrlResolver
) : BundleRepository {

    override suspend fun listBundles(): AppResult<List<BundleSummary>> = safeApiCall {
        api.listBundles().map { it.toDomain() }
    }

    override suspend fun getBundle(slug: String): AppResult<BundleDetail> = safeApiCall {
        api.getBundle(slug).toDomain()
    }

    private fun BundleSummaryResponse.toDomain() = BundleSummary(
        id = id, title = title, slug = slug, description = description,
        product = product.toCatalogDomain(assetUrlResolver), memberCount = memberCount
    )

    private fun BundleDetailResponse.toDomain() = BundleDetail(
        id = id, title = title, slug = slug, description = description,
        product = product.toCatalogDomain(assetUrlResolver), members = members.map { it.toCatalogDomain(assetUrlResolver) }
    )
}
