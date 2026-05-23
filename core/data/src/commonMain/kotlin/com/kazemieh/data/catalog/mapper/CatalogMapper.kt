package com.kazemieh.data.catalog.mapper

import com.kazemieh.domain.model.*
import com.kazemieh.network.dto.catalog.response.*

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
    thumbnailUrl = thumbnailUrl,
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
    url = url,
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
