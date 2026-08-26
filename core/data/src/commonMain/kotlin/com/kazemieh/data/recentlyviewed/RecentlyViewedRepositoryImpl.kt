package com.kazemieh.data.recentlyviewed

import com.kazemieh.common.AppResult
import com.kazemieh.data.catalog.mapper.toCatalogDomain
import com.kazemieh.data.recentlyviewed.source.RecentlyViewedDataSource
import com.kazemieh.domain.catalog.ProductSummary
import com.kazemieh.domain.recentlyviewed.RecentlyViewedRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import com.kazemieh.config.capabilities.AssetUrlResolver

/**
 * پیاده‌سازیِ سروری تاریخچه‌ی بازدید. هر بازدید با productId به سرور ثبت می‌شود و
 * لیست از سرور خوانده می‌شود. با هر ثبتِ جدید، جریانِ [observe] دوباره واکشی می‌کند.
 */
class RecentlyViewedRepositoryImpl(
    private val dataSource: RecentlyViewedDataSource,
    private val assetUrlResolver: AssetUrlResolver
) : RecentlyViewedRepository {

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observe(): Flow<List<ProductSummary>> = refreshTrigger.flatMapLatest {
        flow {
            when (val result = dataSource.getRecentlyViewed(page = 0, size = 12)) {
                is AppResult.Success -> emit(result.data.items.map { it.toCatalogDomain(assetUrlResolver) })
                else -> emit(emptyList())
            }
        }
    }

    override suspend fun add(product: ProductSummary) {
        if (dataSource.record(product.id) is AppResult.Success) {
            refreshTrigger.emit(Unit)
        }
    }
}
