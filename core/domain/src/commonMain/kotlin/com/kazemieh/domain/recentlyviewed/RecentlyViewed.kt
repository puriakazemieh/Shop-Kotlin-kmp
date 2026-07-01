package com.kazemieh.domain.recentlyviewed

import com.kazemieh.domain.catalog.ProductSummary
import kotlinx.coroutines.flow.Flow

/**
 * تاریخچه‌ی محصولاتِ اخیراً بازدیدشده — به‌ازای هر کاربر روی سرور نگهداری می‌شود
 * و بین دستگاه‌ها همگام می‌ماند.
 */
interface RecentlyViewedRepository {
    fun observe(): Flow<List<ProductSummary>>
    suspend fun add(product: ProductSummary)
}

class GetRecentlyViewedUseCase(private val repository: RecentlyViewedRepository) {
    operator fun invoke(): Flow<List<ProductSummary>> = repository.observe()
}

class AddRecentlyViewedUseCase(private val repository: RecentlyViewedRepository) {
    suspend operator fun invoke(product: ProductSummary) = repository.add(product)
}
