package com.kazemieh.data.admin.mapper

import com.kazemieh.domain.model.admin.*
import com.kazemieh.domain.repository.AdminCategory
import com.kazemieh.domain.repository.AdminInventory
import com.kazemieh.domain.repository.AdminPage
import com.kazemieh.domain.repository.Color as DomainColor
import com.kazemieh.domain.repository.Size as DomainSize
import com.kazemieh.network.dto.PageResponse
import com.kazemieh.network.dto.admin.response.*

fun AdminCategoryResponse.toAdminDomain() = AdminCategory(
    id = id,
    name = name,
    slug = slug,
    parentId = parentId
)

fun AdminSizeResponse.toAdminDomain() = DomainSize(
    id = id,
    name = name,
    sortOrder = sortOrder
)

fun AdminColorResponse.toAdminDomain() = DomainColor(
    id = id,
    name = name,
    hex = hex
)

fun AdminProductResponse.toAdminDomain() = AdminProduct(
    id = id,
    categoryId = categoryId,
    title = title,
    slug = slug,
    description = description,
    basePrice = basePrice,
    isActive = isActive
)

fun AdminProductImageResponse.toAdminDomain() = AdminProductImage(
    id = id,
    url = url,
    sortOrder = sortOrder
)

fun AdminVariantResponse.toAdminDomain() = AdminVariant(
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

fun AdminInventoryResponse.toAdminDomain() = AdminInventory(
    variantId = variantId,
    onHand = onHand,
    reserved = reserved,
    version = version
)

fun AdminProductDetailResponse.toAdminDomain() = AdminProductDetail(
    product = product.toAdminDomain(),
    images = images.map { it.toAdminDomain() },
    variants = variants.map { it.toAdminDomain() }
)

fun <T, R> PageResponse<T>.toAdminPage(mapper: (T) -> R) = AdminPage(
    items = items.map(mapper),
    page = page,
    size = size,
    totalElements = totalElements,
    totalPages = totalPages
)
