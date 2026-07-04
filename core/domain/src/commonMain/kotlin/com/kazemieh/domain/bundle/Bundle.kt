package com.kazemieh.domain.bundle

import com.kazemieh.common.AppResult
import com.kazemieh.domain.catalog.ProductSummary

/** باندل/پکیجِ ترکیبیِ محصول. خودِ باندل یک محصولِ واقعیِ قابلِ‌خرید است؛ members فقط نمایشی‌اند. */
data class BundleSummary(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val product: ProductSummary,
    val memberCount: Int
)

data class BundleDetail(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val product: ProductSummary,
    val members: List<ProductSummary>
)

interface BundleRepository {
    suspend fun listBundles(): AppResult<List<BundleSummary>>
    suspend fun getBundle(slug: String): AppResult<BundleDetail>
}

class GetBundlesUseCase(private val repository: BundleRepository) {
    suspend operator fun invoke() = repository.listBundles()
}

class GetBundleDetailUseCase(private val repository: BundleRepository) {
    suspend operator fun invoke(slug: String) = repository.getBundle(slug)
}

// ---------- Admin ----------
data class AdminBundle(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val productId: Long,
    val memberProductIds: List<Long>,
    val isActive: Boolean
)

data class AdminBundleParams(
    val title: String,
    val slug: String,
    val description: String? = null,
    val productId: Long,
    val memberProductIds: List<Long> = emptyList(),
    val isActive: Boolean = true
)

data class AdminBundleUpdateParams(
    val title: String? = null,
    val description: String? = null,
    val memberProductIds: List<Long>? = null,
    val isActive: Boolean? = null
)

interface AdminBundleRepository {
    suspend fun listBundles(): AppResult<List<AdminBundle>>
    suspend fun createBundle(params: AdminBundleParams): AppResult<Long>
    suspend fun updateBundle(id: Long, params: AdminBundleUpdateParams): AppResult<Unit>
    suspend fun deleteBundle(id: Long): AppResult<Unit>
}

class GetAdminBundlesUseCase(private val repository: AdminBundleRepository) {
    suspend operator fun invoke() = repository.listBundles()
}

class CreateBundleUseCase(private val repository: AdminBundleRepository) {
    suspend operator fun invoke(params: AdminBundleParams) = repository.createBundle(params)
}

class UpdateBundleUseCase(private val repository: AdminBundleRepository) {
    suspend operator fun invoke(id: Long, params: AdminBundleUpdateParams) = repository.updateBundle(id, params)
}

class DeleteBundleUseCase(private val repository: AdminBundleRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteBundle(id)
}
