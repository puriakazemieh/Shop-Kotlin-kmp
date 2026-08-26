package com.kazemieh.data.admin.mapper

import com.kazemieh.network.admin.dto.request.*
import com.kazemieh.network.admin.dto.response.*
import com.kazemieh.domain.admin.*
import com.kazemieh.network.common.*
import com.kazemieh.config.capabilities.AssetUrlResolver







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
    brand = brand,
    attributes = attributes.map { com.kazemieh.domain.catalog.ProductAttribute(it.name, it.value) },
    basePrice = basePrice,
    discountedPrice = discountedPrice,
    sku = sku,
    initialOnHand = initialOnHand,
    categoryName = categoryName,
    thumbnailUrl = thumbnailUrl,
    stock = stock,
    isActive = isActive
)

fun AdminProductImageResponse.toAdminDomain(assetUrlResolver: AssetUrlResolver) = AdminProductImage(
    id = id,
    url = assetUrlResolver.resolve(url),
    sortOrder = sortOrder
)

fun AdminProductVideoResponse.toAdminDomain(assetUrlResolver: AssetUrlResolver) = AdminProductVideo(
    id = id,
    url = assetUrlResolver.resolve(url),
    sortOrder = sortOrder
)

fun AdminVariantResponse.toAdminDomain() = AdminVariant(
    id = id,
    sku = sku,
    price = price,
    discountedPrice = discountedPrice,
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

fun AdminProductDetailResponse.toAdminDomain(assetUrlResolver: AssetUrlResolver) = AdminProductDetail(
    product = product.toAdminDomain(),
    images = images.map { it.toAdminDomain(assetUrlResolver) },
    videos = videos.map { it.toAdminDomain(assetUrlResolver) },
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

fun AdminInteractionResponse.toAdminDomain() = AdminInteraction(
    id = id,
    productId = productId,
    productTitle = productTitle,
    userId = userId,
    userName = userName,
    content = content,
    rating = rating,
    isNew = isNew,
    createdAt = createdAt
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
    page = page.number,
    size = page.size,
    totalElements = page.totalElements,
    totalPages = page.totalPages
)
