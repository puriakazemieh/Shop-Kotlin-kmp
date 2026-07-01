package com.kazemieh.domain.recentlyviewed

import com.kazemieh.domain.catalog.ProductSummary
import kotlinx.coroutines.flow.Flow

/**
 * تاریخچه‌ی محصولاتِ اخیراً بازدیدشده — به‌صورت محلی روی همان دستگاه نگهداری می‌شود
 * (هم‌سبک با نگهداریِ «استوری‌های دیده‌شده»). هر دستگاه تاریخچه‌ی مستقلِ خود را دارد.
 */
interface RecentlyViewedRepository {
    fun observe(): Flow<List<ProductSummary>>
    suspend fun add(product: ProductSummary)
    suspend fun clear()
}

class GetRecentlyViewedUseCase(private val repository: RecentlyViewedRepository) {
    operator fun invoke(): Flow<List<ProductSummary>> = repository.observe()
}

class AddRecentlyViewedUseCase(private val repository: RecentlyViewedRepository) {
    suspend operator fun invoke(product: ProductSummary) = repository.add(product)
}
