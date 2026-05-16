package com.kazemieh.data.catalog.mapper

import com.kazemieh.domain.model.*
import com.kazemieh.domain.repository.Color
import com.kazemieh.domain.repository.Size
import com.kazemieh.network.dto.catalog.response.*

fun CategoryResponse.toDomain(): Category = Category(
    id = id,
    name = name,
    slug = slug,
    parentId = parentId
)

fun SizeResponse.toDomain() = Size(
    id = id,
    name = name,
    sortOrder = sortOrder
)

fun ColorResponse.toDomain() = Color(
    id = id,
    name = name,
    hex = hex
)

fun ProductSummaryResponse.toDomain() = ProductSummary(
    id = id,
    title = title,
    slug = slug,
    thumbnailUrl = thumbnailUrl,
    minPrice = minPrice,
    maxPrice = maxPrice,
    inStock = inStock,
    categoryId = categoryId,
    categoryName = categoryName
)

fun ProductDetailResponse.toDomain() = ProductDetail(
    id = id,
    title = title,
    slug = slug,
    description = description,
    categoryId = categoryId,
    categoryName = categoryName,
    images = images.map { it.toDomain() },
    variants = variants.map { it.toDomain() },
    createdAt = createdAt
)

fun ProductImageResponse.toDomain() = ProductImage(
    id = id,
    url = url,
    sortOrder = sortOrder
)

fun ProductVariantResponse.toDomain() = ProductVariant(
    id = id,
    sku = sku,
    price = price,
    compareAtPrice = compareAtPrice,
    available = available,
    sizeName = sizeName,
    colorName = colorName
)
