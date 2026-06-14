package com.kazemieh.data.catalog.mapper

import com.kazemieh.network.catalog.dto.response.*
import com.kazemieh.domain.catalog.*
import com.kazemieh.domain.admin.AdminPage
import com.kazemieh.network.common.*
import com.kazemieh.common.*







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
    minDiscountedPrice = minDiscountedPrice,
    maxDiscountedPrice = maxDiscountedPrice,
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
    discountedPrice = discountedPrice,
    compareAtPrice = compareAtPrice,
    available = availableQty,
    options = options
)
