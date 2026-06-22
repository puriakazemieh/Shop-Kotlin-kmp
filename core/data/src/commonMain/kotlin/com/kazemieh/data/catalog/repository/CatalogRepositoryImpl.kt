package com.kazemieh.data.catalog.repository

import com.kazemieh.network.catalog.dto.response.*
import com.kazemieh.domain.catalog.*
import com.kazemieh.domain.admin.AdminPage
import com.kazemieh.network.common.*
import com.kazemieh.common.*
import com.kazemieh.data.catalog.mapper.*
import com.kazemieh.data.admin.mapper.toAdminPage
import com.kazemieh.data.catalog.source.CatalogDataSource
import com.kazemieh.domain.favorite.FavoriteRepository


class CatalogRepositoryImpl(
    private val dataSource: CatalogDataSource,
    private val favoriteRepository: FavoriteRepository
) : CatalogRepository {

    override suspend fun getCategories(): AppResult<List<Category>> =
        dataSource.getCategories().map { list -> list.map { it.toCatalogDomain() } }

    override suspend fun getProducts(
        query: String?,
        categoryId: Long?,
        options: Map<String, String>?,
        minPrice: Double?,
        maxPrice: Double?,
        inStock: Boolean?,
        page: Int,
        size: Int,
        sort: String?
    ): AppResult<AdminPage<ProductSummary>> =
        dataSource.getProducts(query, categoryId, options, minPrice, maxPrice, inStock, page, size, sort)
            .map { pageResponse -> 
                val adminPage = pageResponse.toAdminPage { dto -> dto.toCatalogDomain() }
                adminPage.items.forEach { 
                    favoriteRepository.updateFavoriteStatus(it.id, it.isFavorite)
                }
                adminPage
            }

    override suspend fun getProductDetail(slug: String): AppResult<ProductDetail> =
        dataSource.getProductDetail(slug).map { 
            val product = it.toCatalogDomain()
            favoriteRepository.updateFavoriteStatus(product.id, product.isFavorite)
            product
        }
}
