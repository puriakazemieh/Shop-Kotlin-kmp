package com.kazemieh.data.catalog.repository

import com.kazemieh.common.AppResult
import com.kazemieh.data.admin.mapper.toAdminPage
import com.kazemieh.data.admin.repository.map
import com.kazemieh.data.catalog.mapper.toCatalogDomain
import com.kazemieh.data.catalog.source.CatalogDataSource
import com.kazemieh.domain.model.*
import com.kazemieh.domain.repository.*

class CatalogRepositoryImpl(
    private val dataSource: CatalogDataSource
) : CatalogRepository {

    override suspend fun getCategories(): AppResult<List<Category>> =
        dataSource.getCategories().map { list -> list.map { it.toCatalogDomain() } }

    override suspend fun getSizes(): AppResult<List<Size>> =
        dataSource.getSizes().map { list -> list.map { it.toCatalogDomain() } }

    override suspend fun getColors(): AppResult<List<Color>> =
        dataSource.getColors().map { list -> list.map { it.toCatalogDomain() } }

    override suspend fun getProducts(
        query: String?,
        categoryId: Long?,
        sizeId: Long?,
        colorId: Long?,
        minPrice: Double?,
        maxPrice: Double?,
        inStock: Boolean?,
        page: Int,
        size: Int,
        sort: String?
    ): AppResult<AdminPage<ProductSummary>> =
        dataSource.getProducts(query, categoryId, sizeId, colorId, minPrice, maxPrice, inStock, page, size, sort)
            .map { it.toAdminPage { dto -> dto.toCatalogDomain() } }

    override suspend fun getProductDetail(slug: String): AppResult<ProductDetail> =
        dataSource.getProductDetail(slug).map { it.toCatalogDomain() }
}
