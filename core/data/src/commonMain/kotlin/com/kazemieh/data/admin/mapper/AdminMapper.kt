package com.kazemieh.data.admin.mapper

import com.kazemieh.domain.model.admin.*
import com.kazemieh.domain.repository.*
import com.kazemieh.network.PlatformConfig
import com.kazemieh.network.dto.PageResponse
import com.kazemieh.network.dto.admin.response.*

fun AdminCategoryResponse.toAdminDomain() = AdminCategory(
    id = id,
    name = name,
    slug = slug,
    parentId = parentId
)

fun AdminSizeResponse.toAdminDomain() = Size(
    id = id,
    name = name,
    sortOrder = sortOrder
)

fun AdminColorResponse.toAdminDomain() = Color(
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
    url = if (url.startsWith("http")) url else "${PlatformConfig.baseUrl.removeSuffix("/")}$url",
    sortOrder = sortOrder
)

fun AdminVariantResponse.toAdminDomain() = AdminVariant(
    id = id,
    sku = sku,
    price = price,
    compareAtPrice = compareAtPrice,
    isActive = isActive,
    onHand = inventory?.onHand ?: 0,
    reserved = inventory?.reserved ?: 0,
    sizeId = sizeId,
    sizeName = sizeName,
    colorId = colorId,
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

fun AdminOrderSummaryResponse.toAdminDomain() = AdminOrderSummary(
    id = id,
    userId = userId,
    userEmail = userEmail,
    status = status,
    totalPrice = totalPrice,
    createdAt = createdAt
)

fun AdminOrderDetailResponse.toAdminDomain() = AdminOrderDetail(
    id = id,
    userId = userId,
    userEmail = userEmail,
    status = status,
    subtotalPrice = subtotalPrice,
    shippingPrice = shippingPrice,
    totalPrice = totalPrice,
    createdAt = createdAt,
    updatedAt = updatedAt,
    addressSnapshot = addressSnapshot.toAdminDomain(),
    items = items.map { it.toAdminDomain() }
)

fun AdminOrderItemResponse.toAdminDomain() = AdminOrderItem(
    id = id,
    variantId = variantId,
    qty = qty,
    unitPriceSnapshot = unitPriceSnapshot,
    titleSnapshot = titleSnapshot,
    sizeSnapshot = sizeSnapshot,
    colorSnapshot = colorSnapshot
)

fun AdminAddressSnapshotResponse.toAdminDomain() = AdminAddressSnapshot(
    receiverName = receiverName,
    receiverPhone = receiverPhone,
    country = country,
    province = province,
    city = city,
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    postalCode = postalCode
)

fun <T, R> PageResponse<T>.toAdminPage(mapper: (T) -> R) = AdminPage(
    items = items.map(mapper),
    page = page,
    size = size,
    totalElements = totalElements,
    totalPages = totalPages
)
