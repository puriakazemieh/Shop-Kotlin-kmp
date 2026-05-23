package com.kazemieh.data.admin.repository

import com.kazemieh.common.AppResult
import com.kazemieh.data.admin.mapper.*
import com.kazemieh.data.admin.source.AdminDataSource
import com.kazemieh.domain.model.admin.*
import com.kazemieh.domain.repository.*
import com.kazemieh.network.dto.admin.request.*

class AdminRepositoryImpl(
    private val dataSource: AdminDataSource
) : AdminRepository {

    override suspend fun listCategories(): AppResult<List<AdminCategory>> =
        dataSource.listCategories().map { list -> list.map { it.toAdminDomain() } }

    override suspend fun createCategory(name: String, slug: String, parentId: Long?): AppResult<AdminCategory> =
        dataSource.createCategory(AdminCreateCategoryRequest(name, slug, parentId)).map { it.toAdminDomain() }

    override suspend fun updateCategory(id: Long, name: String?, slug: String?, parentId: Long?): AppResult<AdminCategory> =
        dataSource.updateCategory(id, AdminUpdateCategoryRequest(name, slug, parentId)).map { it.toAdminDomain() }

    override suspend fun deleteCategory(id: Long): AppResult<Unit> =
        dataSource.deleteCategory(id)

    override suspend fun listProducts(page: Int, size: Int, includeInactive: Boolean, query: String?): AppResult<AdminPage<AdminProduct>> =
        dataSource.listProducts(page, size, includeInactive, query).map { it.toAdminPage { dto -> dto.toAdminDomain() } }

    override suspend fun createProduct(
        categoryId: Long?,
        title: String,
        slug: String,
        description: String?,
        basePrice: Double?,
        isActive: Boolean,
        variants: List<AdminCreateVariant>?
    ): AppResult<AdminProduct> =
        dataSource.createProduct(
            AdminCreateProductRequest(
                categoryId,
                title,
                slug,
                description,
                basePrice,
                isActive,
                variants?.map {
                    AdminCreateVariantRequest(
                        it.options,
                        it.sku,
                        it.price,
                        it.compareAtPrice,
                        it.isActive,
                        it.initialOnHand
                    )
                }
            )
        ).map { it.toAdminDomain() }

    override suspend fun getProductDetail(id: Long): AppResult<AdminProductDetail> =
        dataSource.getProductDetail(id).map { it.toAdminDomain() }

    override suspend fun updateProduct(
        id: Long,
        categoryId: Long?,
        title: String?,
        slug: String?,
        description: String?,
        basePrice: Double?,
        isActive: Boolean?
    ): AppResult<AdminProduct> =
        dataSource.updateProduct(id, AdminUpdateProductRequest(categoryId, title, slug, description, basePrice, isActive)).map { it.toAdminDomain() }

    override suspend fun deleteProduct(id: Long): AppResult<Unit> =
        dataSource.deleteProduct(id)

    override suspend fun addImage(productId: Long, bytes: ByteArray, sortOrder: Int?): AppResult<AdminProductImage> =
        dataSource.addImage(productId, bytes, sortOrder).map { it.toAdminDomain() }

    override suspend fun reorderImages(productId: Long, items: List<Pair<Long, Int>>): AppResult<List<AdminProductImage>> =
        dataSource.reorderImages(productId, AdminReorderImagesRequest(items.map { ReorderItem(it.first, it.second) })).map { list -> list.map { it.toAdminDomain() } }

    override suspend fun deleteImage(productId: Long, imageId: Long): AppResult<Unit> =
        dataSource.deleteImage(productId, imageId)

    override suspend fun createVariant(
        productId: Long,
        options: Map<String, String>,
        sku: String,
        price: Double,
        compareAtPrice: Double?,
        isActive: Boolean,
        initialOnHand: Int
    ): AppResult<AdminVariant> =
        dataSource.createVariant(productId, AdminCreateVariantRequest(options, sku, price, compareAtPrice, isActive, initialOnHand)).map { it.toAdminDomain() }

    override suspend fun updateVariant(
        variantId: Long,
        sku: String?,
        price: Double?,
        compareAtPrice: Double?,
        options: Map<String, String>?,
        isActive: Boolean?
    ): AppResult<AdminVariant> =
        dataSource.updateVariant(variantId, AdminUpdateVariantRequest(sku, price, compareAtPrice, options, isActive)).map { it.toAdminDomain() }

    override suspend fun deleteVariant(variantId: Long): AppResult<Unit> =
        dataSource.deleteVariant(variantId)

    override suspend fun getInventory(variantId: Long): AppResult<AdminInventory> =
        dataSource.getInventory(variantId).map { it.toAdminDomain() }

    override suspend fun setInventory(variantId: Long, onHand: Int, version: Int?): AppResult<AdminInventory> =
        dataSource.setInventory(variantId, AdminInventorySetRequest(onHand, version)).map { it.toAdminDomain() }

    override suspend fun adjustInventory(variantId: Long, delta: Int, version: Int?): AppResult<AdminInventory> =
        dataSource.adjustInventory(variantId, AdminInventoryAdjustRequest(delta, version)).map { it.toAdminDomain() }

    override suspend fun listOrders(
        status: String?,
        userId: Long?,
        page: Int,
        size: Int
    ): AppResult<AdminPage<AdminOrderSummary>> =
        dataSource.listOrders(status, userId, page, size).map { it.toAdminPage { dto -> dto.toAdminDomain() } }

    override suspend fun getOrderDetail(id: Long): AppResult<AdminOrderDetail> =
        dataSource.getOrderDetail(id).map { it.toAdminDomain() }

    override suspend fun updateOrderStatus(id: Long, status: String): AppResult<Unit> =
        dataSource.updateOrderStatus(id, AdminUpdateOrderStatusRequest(status))
}
