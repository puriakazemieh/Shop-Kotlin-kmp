package com.kazemieh.data.admin.mapper

import com.kazemieh.domain.model.admin.*
import com.kazemieh.domain.repository.AdminCategory
import com.kazemieh.domain.repository.AdminInventory
import com.kazemieh.domain.repository.AdminPage
import com.kazemieh.network.dto.PageResponse
import com.kazemieh.network.dto.admin.response.*

fun AdminCategoryResponse.toDomain() = AdminCategory(
    id = id,
    name = name,
    slug = slug,
    parentId = parentId
)

fun AdminProductResponse.toDomain() = AdminProduct(
    id = id,
    categoryId = categoryId,
    title = title,
    slug = slug,
    description = description,
    basePrice = basePrice,
    isActive = isActive
)

fun AdminProductImageResponse.toDomain() = AdminProductImage(
    id = id,
    url = url,
    sortOrder = sortOrder
)

fun AdminVariantResponse.toDomain() = AdminVariant(
    id = id,
    sku = sku,
    price = price,
    compareAtPrice = compareAtPrice,
    isActive = isActive,
    onHand = onHand,
    reserved = reserved,
    sizeName = sizeName,
    colorName = colorName
)

fun AdminInventoryResponse.toDomain() = AdminInventory(
    variantId = variantId,
    onHand = onHand,
    reserved = reserved,
    version = version
)

fun AdminProductDetailResponse.toDomain() = AdminProductDetail(
    product = product.toDomain(),
    images = images.map { it.toDomain() },
    variants = variants.map { it.toDomain() }
)

fun <T, R> PageResponse<T>.toDomain(mapper: (T) -> R) = AdminPage(
    items = items.map(mapper),
    page = page,
    size = size,
    totalElements = totalElements,
    totalPages = totalPages
)
