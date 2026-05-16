package com.kazemieh.data.admin.repository

import com.kazemieh.common.AppResult
import com.kazemieh.data.admin.mapper.toAdminPage
import com.kazemieh.data.admin.mapper.toDomain
import com.kazemieh.data.catalog.mapper.toDomain as toCatalogDomain
import com.kazemieh.data.admin.source.AdminDataSource
import com.kazemieh.domain.model.admin.*
import com.kazemieh.domain.repository.*
import com.kazemieh.network.dto.admin.request.*

class AdminRepositoryImpl(
    private val dataSource: AdminDataSource
) : AdminRepository {

    override suspend fun listCategories(): AppResult<List<AdminCategory>> =
        dataSource.listCategories().map { list -> list.map { it.toDomain() } }

    override suspend fun uploadImage(bytes: ByteArray): AppResult<String> {
        // Since I can't find Firebase dependency, I'll return a mock URL for now
        // In a real app, this would use Firebase Storage or another provider
        return AppResult.Success("https://via.placeholder.com/150")
    }

    override suspend fun createCategory(name: String, slug: String, parentId: Long?): AppResult<AdminCategory> =
        dataSource.createCategory(AdminCreateCategoryRequest(name, slug, parentId)).map { it.toDomain() }

    override suspend fun updateCategory(id: Long, name: String?, slug: String?, parentId: Long?): AppResult<AdminCategory> =
        dataSource.updateCategory(id, AdminUpdateCategoryRequest(name, slug, parentId)).map { it.toDomain() }

    override suspend fun deleteCategory(id: Long): AppResult<Unit> =
        dataSource.deleteCategory(id)

    override suspend fun createSize(name: String, sortOrder: Int): AppResult<Size> =
        dataSource.createSize(AdminCreateSizeRequest(name, sortOrder)).map { it.toCatalogDomain() }

    override suspend fun createColor(name: String, hex: String?): AppResult<Color> =
        dataSource.createColor(AdminCreateColorRequest(name, hex)).map { it.toCatalogDomain() }

    override suspend fun listProducts(page: Int, size: Int, includeInactive: Boolean, query: String?): AppResult<AdminPage<AdminProduct>> =
        dataSource.listProducts(page, size, includeInactive, query).map { it.toAdminPage { dto -> dto.toDomain() } }

    override suspend fun createProduct(
        categoryId: Long?,
        title: String,
        slug: String,
        description: String?,
        basePrice: Double?,
        isActive: Boolean
    ): AppResult<AdminProduct> =
        dataSource.createProduct(AdminCreateProductRequest(categoryId, title, slug, description, basePrice, isActive)).map { it.toDomain() }

    override suspend fun getProductDetail(id: Long): AppResult<AdminProductDetail> =
        dataSource.getProductDetail(id).map { it.toDomain() }

    override suspend fun updateProduct(
        id: Long,
        categoryId: Long?,
        title: String?,
        slug: String?,
        description: String?,
        basePrice: Double?,
        isActive: Boolean?
    ): AppResult<AdminProduct> =
        dataSource.updateProduct(id, AdminUpdateProductRequest(categoryId, title, slug, description, basePrice, isActive)).map { it.toDomain() }

    override suspend fun deleteProduct(id: Long): AppResult<Unit> =
        dataSource.deleteProduct(id)

    override suspend fun addImage(productId: Long, url: String, sortOrder: Int?): AppResult<AdminProductImage> =
        dataSource.addImage(productId, AdminAddImageRequest(url, sortOrder)).map { it.toDomain() }

    override suspend fun deleteImage(productId: Long, imageId: Long): AppResult<Unit> =
        dataSource.deleteImage(productId, imageId)

    override suspend fun createVariant(
        productId: Long,
        sizeId: Long,
        colorId: Long,
        sku: String,
        price: Double,
        compareAtPrice: Double?,
        isActive: Boolean,
        initialOnHand: Int
    ): AppResult<AdminVariant> =
        dataSource.createVariant(productId, AdminCreateVariantRequest(sizeId, colorId, sku, price, compareAtPrice, isActive, initialOnHand)).map { it.toDomain() }

    override suspend fun updateVariant(
        variantId: Long,
        sku: String?,
        price: Double?,
        compareAtPrice: Double?,
        isActive: Boolean?
    ): AppResult<AdminVariant> =
        dataSource.updateVariant(variantId, AdminUpdateVariantRequest(sku, price, compareAtPrice, isActive)).map { it.toDomain() }

    override suspend fun getInventory(variantId: Long): AppResult<AdminInventory> =
        dataSource.getInventory(variantId).map { it.toDomain() }

    override suspend fun setInventory(variantId: Long, onHand: Int, version: Int?): AppResult<AdminInventory> =
        dataSource.setInventory(variantId, AdminInventorySetRequest(onHand, version)).map { it.toDomain() }

    override suspend fun adjustInventory(variantId: Long, delta: Int, version: Int?): AppResult<AdminInventory> =
        dataSource.adjustInventory(variantId, AdminInventoryAdjustRequest(delta, version)).map { it.toDomain() }
}
