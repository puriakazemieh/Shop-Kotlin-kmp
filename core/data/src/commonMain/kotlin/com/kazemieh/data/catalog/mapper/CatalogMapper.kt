package com.kazemieh.data.catalog.mapper

import com.kazemieh.network.catalog.dto.response.*
import com.kazemieh.domain.catalog.*
import com.kazemieh.domain.admin.AdminPage
import com.kazemieh.network.common.*
import com.kazemieh.common.*
import com.kazemieh.config.capabilities.AssetUrlResolver







fun CategoryResponse.toCatalogDomain(): Category = Category(
    id = id,
    name = name,
    slug = slug,
    parentId = parentId
)

fun ProductSummaryResponse.toCatalogDomain(assetUrlResolver: AssetUrlResolver) = ProductSummary(
    id = id,
    title = title,
    slug = slug,
    thumbnailUrl = thumbnailUrl?.let(assetUrlResolver::resolve),
    minPrice = minPrice,
    maxPrice = maxPrice,
    minDiscountedPrice = minDiscountedPrice,
    maxDiscountedPrice = maxDiscountedPrice,
    inStock = inStock,
    categoryId = categoryId,
    categoryName = categoryName,
    options = options,
    isFavorite = isFavorite,
    averageRating = averageRating,
    reviewCount = reviewCount
)

fun CampaignResponse.toCampaignDomain(assetUrlResolver: AssetUrlResolver) = Campaign(
    id = id,
    title = title,
    endsAt = endsAt,
    remainingSeconds = remainingSeconds,
    products = products.map { it.toCatalogDomain(assetUrlResolver) }
)

fun BannerResponse.toBannerDomain(assetUrlResolver: AssetUrlResolver) = Banner(
    id = id,
    title = title,
    subtitle = subtitle,
    imageUrl = imageUrl?.let(assetUrlResolver::resolve),
    categoryId = categoryId
)

fun ProductDetailResponse.toCatalogDomain(assetUrlResolver: AssetUrlResolver) = ProductDetail(
    id = id,
    title = title,
    slug = slug,
    description = description,
    brand = brand,
    attributes = attributes.map { com.kazemieh.domain.catalog.ProductAttribute(it.name, it.value) },
    categoryId = categoryId,
    categoryName = categoryName,
    images = images.map { it.toCatalogDomain(assetUrlResolver) },
    videos = videos.map { it.toCatalogDomain(assetUrlResolver) },
    variants = variants.map { it.toCatalogDomain() },
    createdAt = createdAt,
    isFavorite = isFavorite
)

fun ProductImageResponse.toCatalogDomain(assetUrlResolver: AssetUrlResolver) = ProductImage(
    id = id ?: 0L,
    url = assetUrlResolver.resolve(url),
    sortOrder = sortOrder
)

fun ProductVideoResponse.toCatalogDomain(assetUrlResolver: AssetUrlResolver) = ProductVideo(
    id = id ?: 0L,
    url = assetUrlResolver.resolve(url),
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
