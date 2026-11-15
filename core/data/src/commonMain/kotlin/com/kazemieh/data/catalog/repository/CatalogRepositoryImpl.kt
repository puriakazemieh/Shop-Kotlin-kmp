package com.kazemieh.data.catalog.repository

import com.kazemieh.network.catalog.dto.response.*
import com.kazemieh.domain.catalog.*
import com.kazemieh.domain.admin.AdminPage
import com.kazemieh.network.common.*
import com.kazemieh.common.*
import com.kazemieh.data.catalog.mapper.*
import com.kazemieh.data.admin.mapper.toAdminPage
import com.kazemieh.data.catalog.source.CatalogDataSource
import com.kazemieh.data.common.guardOrError
import com.kazemieh.domain.favorite.FavoriteRepository
import com.kazemieh.config.capabilities.AssetUrlResolver
import com.kazemieh.config.capabilities.FeatureUseCaseGuard

class CatalogRepositoryImpl(
    private val dataSource: CatalogDataSource,
    private val favoriteRepository: FavoriteRepository,
    private val assetUrlResolver: AssetUrlResolver,
    private val guard: FeatureUseCaseGuard? = null
) : CatalogRepository {

    override suspend fun getCategories(): AppResult<List<Category>> =
        guard.guardOrError("commerce.core") {
            dataSource.getCategories().map { list -> list.map { it.toCatalogDomain() } }
        }

    override suspend fun getProducts(
        query: String?,
        categoryId: Long?,
        options: Map<String, String>?,
        minPrice: Double?,
        maxPrice: Double?,
        inStock: Boolean?,
        page: Int,
        size: Int,
        sort: String?,
        discountedOnly: Boolean
    ): AppResult<AdminPage<ProductSummary>> =
        guard.guardOrError("commerce.core") {
            dataSource.getProducts(query, categoryId, options, minPrice, maxPrice, inStock, page, size, sort, discountedOnly)
                .map { pageResponse -> 
                    val adminPage = pageResponse.toAdminPage { dto -> dto.toCatalogDomain(assetUrlResolver) }
                    adminPage.items.forEach { 
                        favoriteRepository.updateFavoriteStatus(it.id, it.isFavorite)
                    }
                    adminPage
                }
        }

    override suspend fun getProductDetail(slug: String): AppResult<ProductDetail> =
        guard.guardOrError("commerce.core") {
            dataSource.getProductDetail(slug).map {
                val product = it.toCatalogDomain(assetUrlResolver)
                favoriteRepository.updateFavoriteStatus(product.id, product.isFavorite)
                product
            }
        }

    override suspend fun getActiveCampaign(): AppResult<Campaign?> =
        guard.guardOrError("commerce.core") {
            dataSource.getActiveCampaign().map { response -> response?.toCampaignDomain(assetUrlResolver) }
        }

    override suspend fun getBanners(): AppResult<List<Banner>> =
        guard.guardOrError("commerce.core") {
            dataSource.getBanners().map { list -> list.map { it.toBannerDomain(assetUrlResolver) } }
        }

    override suspend fun requestBackInStock(productId: Long, variantId: Long): AppResult<Unit> =
        guard.guardOrError("commerce.core") {
            dataSource.requestBackInStock(productId, variantId)
        }

    override suspend fun getFrequentlyBoughtTogether(productId: Long): AppResult<List<ProductSummary>> =
        guard.guardOrError("commerce.core") {
            dataSource.getFrequentlyBoughtTogether(productId).map { list -> list.map { it.toCatalogDomain(assetUrlResolver) } }
        }

    override suspend fun subscribeToPriceAlert(productId: Long, variantId: Long, targetPrice: Double): AppResult<Unit> =
        guard.guardOrError("commerce.core") {
            dataSource.subscribeToPriceAlert(productId, variantId, targetPrice)
        }
}
