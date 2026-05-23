package com.kazemieh.data.catalog.mapper

import com.kazemieh.domain.model.Category
import com.kazemieh.domain.model.ProductDetail
import com.kazemieh.domain.model.ProductImage
import com.kazemieh.domain.model.ProductSummary
import com.kazemieh.domain.model.ProductVariant
import com.kazemieh.network.PlatformConfig
import com.kazemieh.network.dto.catalog.response.CategoryResponse
import com.kazemieh.network.dto.catalog.response.ProductDetailResponse
import com.kazemieh.network.dto.catalog.response.ProductImageResponse
import com.kazemieh.network.dto.catalog.response.ProductSummaryResponse
import com.kazemieh.network.dto.catalog.response.ProductVariantResponse

fun CategoryResponse.toCatalogDomain(): Category = Category(
    id = id,
    name = name,
    slug = slug,
    parentId = parentId
)

fun ProductSummaryResponse.toCatalogDomain() = ProductSummary(
    id = id,
    title = title,
    slug = slug,
    thumbnailUrl = if (thumbnailUrl?.startsWith("http") == true) thumbnailUrl else "${PlatformConfig.baseUrl.removeSuffix("/")}$thumbnailUrl",
    minPrice = minPrice,
    maxPrice = maxPrice,
    inStock = inStock,
    categoryId = categoryId,
    categoryName = categoryName,
    options = options
)

fun ProductDetailResponse.toCatalogDomain() = ProductDetail(
    id = id,
    title = title,
    slug = slug,
    description = description,
    categoryId = categoryId,
    categoryName = categoryName,
    images = images.map { it.toCatalogDomain() },
    variants = variants.map { it.toCatalogDomain() },
    createdAt = createdAt
)

fun ProductImageResponse.toCatalogDomain() = ProductImage(
    id = id ?: 0L,
    url =  if (url.startsWith("http")) url else "${PlatformConfig.baseUrl.removeSuffix("/")}$url",
    sortOrder = sortOrder
)

fun ProductVariantResponse.toCatalogDomain() = ProductVariant(
    id = id,
    sku = sku,
    price = price,
    compareAtPrice = compareAtPrice,
    available = availableQty,
    options = options
)
