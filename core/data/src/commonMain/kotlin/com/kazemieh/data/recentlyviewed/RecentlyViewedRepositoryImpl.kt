package com.kazemieh.data.recentlyviewed

import com.kazemieh.domain.catalog.ProductSummary
import com.kazemieh.domain.recentlyviewed.RecentlyViewedRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * پیاده‌سازیِ محلیِ تاریخچه‌ی بازدید با ذخیره‌سازیِ JSON در Settings.
 * حداکثر [MAX_ITEMS] موردِ اخیر نگه داشته می‌شود؛ موردِ تکراری به ابتدای لیست منتقل می‌شود.
 */
class RecentlyViewedRepositoryImpl(
    private val settings: Settings
) : RecentlyViewedRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val key = "recently_viewed_products"
    private val trigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    override fun observe(): Flow<List<ProductSummary>> = trigger.map { read().map { it.toDomain() } }

    override suspend fun add(product: ProductSummary) {
        val current = read().filterNot { it.id == product.id }
        val updated = (listOf(product.toDto()) + current).take(MAX_ITEMS)
        settings.putString(key, json.encodeToString(updated))
        trigger.emit(Unit)
    }

    override suspend fun clear() {
        settings.remove(key)
        trigger.emit(Unit)
    }

    private fun read(): List<RecentProductDto> {
        val raw = settings.getStringOrNull(key) ?: return emptyList()
        return runCatching { json.decodeFromString<List<RecentProductDto>>(raw) }.getOrDefault(emptyList())
    }

    companion object {
        private const val MAX_ITEMS = 12
    }
}

@Serializable
private data class RecentProductDto(
    val id: Long,
    val title: String,
    val slug: String,
    val thumbnailUrl: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minDiscountedPrice: Double? = null,
    val maxDiscountedPrice: Double? = null,
    val inStock: Boolean = true,
    val categoryId: Long? = null,
    val categoryName: String? = null
)

private fun ProductSummary.toDto() = RecentProductDto(
    id = id,
    title = title,
    slug = slug,
    thumbnailUrl = thumbnailUrl,
    minPrice = minPrice,
    maxPrice = maxPrice,
    minDiscountedPrice = minDiscountedPrice,
    maxDiscountedPrice = maxDiscountedPrice,
    inStock = inStock,
    categoryId = categoryId,
    categoryName = categoryName
)

private fun RecentProductDto.toDomain() = ProductSummary(
    id = id,
    title = title,
    slug = slug,
    thumbnailUrl = thumbnailUrl,
    minPrice = minPrice,
    maxPrice = maxPrice,
    minDiscountedPrice = minDiscountedPrice,
    maxDiscountedPrice = maxDiscountedPrice,
    inStock = inStock,
    categoryId = categoryId,
    categoryName = categoryName,
    options = emptyMap()
)
