package com.kazemieh.data.admin.mapper

import com.kazemieh.network.admin.dto.request.*
import com.kazemieh.network.admin.dto.response.*
import com.kazemieh.domain.admin.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*







fun AdminCategoryResponse.toAdminDomain() = AdminCategory(
    id = id,
    name = name,
    slug = slug,
    parentId = parentId
)

fun AdminProductResponse.toAdminDomain() = AdminProduct(
    id = id,
    categoryId = categoryId,
    title = title,
    slug = slug,
    description = description,
    basePrice = basePrice,
    discountedPrice = discountedPrice,
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
    options = options
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
    id = id ?: 0L,
    variantId = variantId,
    qty = qty,
    unitPriceSnapshot = unitPriceSnapshot,
    titleSnapshot = titleSnapshot,
    optionsSnapshot = optionsSnapshot
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

fun AdminOptionResponse.toAdminDomain() = AdminOption(
    id = id,
    name = name,
    values = values.map { it.toAdminDomain() }
)

fun AdminOptionValueResponse.toAdminDomain() = AdminOptionValue(
    id = id,
    value = value
)

fun AdminDiscountResponse.toAdminDomain() = Discount(
    id = id,
    code = code,
    type = if (type == "PERCENTAGE") DiscountType.PERCENTAGE else DiscountType.FIXED_AMOUNT,
    value = value,
    maxDiscountAmount = maxDiscountAmount,
    minOrderAmount = minOrderAmount,
    startDate = startDate,
    endDate = endDate,
    usageLimit = usageLimit,
    usageCount = usageCount,
    isActive = isActive
)

fun CreateDiscountParam.toRequest() = AdminCreateDiscountRequest(
    code = code,
    type = type.name,
    value = value,
    maxDiscountAmount = maxDiscountAmount,
    minOrderAmount = minOrderAmount,
    startDate = startDate,
    endDate = endDate,
    usageLimit = usageLimit,
    isActive = isActive
)

fun UpdateDiscountParam.toRequest() = AdminUpdateDiscountRequest(
    code = code,
    type = type?.name,
    value = value,
    maxDiscountAmount = maxDiscountAmount,
    minOrderAmount = minOrderAmount,
    startDate = startDate,
    endDate = endDate,
    usageLimit = usageLimit,
    isActive = isActive
)

fun <T, R> PageResponse<T>.toAdminPage(mapper: (T) -> R) = AdminPage(
    items = items.map(mapper),
    page = page,
    size = size,
    totalElements = totalElements,
    totalPages = totalPages
)
