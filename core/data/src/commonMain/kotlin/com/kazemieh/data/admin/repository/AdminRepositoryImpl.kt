package com.kazemieh.data.admin.repository

import com.kazemieh.common.AppResult
import com.kazemieh.data.admin.mapper.toAdminDomain
import com.kazemieh.data.admin.mapper.toAdminPage
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

    override suspend fun listSizes(): AppResult<List<Size>> =
        dataSource.listSizes().map { list -> list.map { it.toAdminDomain() } }

    override suspend fun createSize(name: String, sortOrder: Int): AppResult<Size> =
        dataSource.createSize(AdminCreateSizeRequest(name, sortOrder)).map { it.toAdminDomain() }

    override suspend fun updateSize(id: Long, name: String?, sortOrder: Int?): AppResult<Size> =
        dataSource.updateSize(id, AdminUpdateSizeRequest(name, sortOrder)).map { it.toAdminDomain() }

    override suspend fun deleteSize(id: Long): AppResult<Unit> =
        dataSource.deleteSize(id)

    override suspend fun listColors(): AppResult<List<Color>> =
        dataSource.listColors().map { list -> list.map { it.toAdminDomain() } }

    override suspend fun createColor(name: String, hex: String?): AppResult<Color> =
        dataSource.createColor(AdminCreateColorRequest(name, hex)).map { it.toAdminDomain() }

    override suspend fun updateColor(id: Long, name: String?, hex: String?): AppResult<Color> =
        dataSource.updateColor(id, AdminUpdateColorRequest(name, hex)).map { it.toAdminDomain() }

    override suspend fun deleteColor(id: Long): AppResult<Unit> =
        dataSource.deleteColor(id)

    override suspend fun listProducts(page: Int, size: Int, includeInactive: Boolean, query: String?): AppResult<AdminPage<AdminProduct>> =
        dataSource.listProducts(page, size, includeInactive, query).map { it.toAdminPage { dto -> dto.toAdminDomain() } }

    override suspend fun createProduct(
        categoryId: Long?,
        title: String,
        slug: String,
        description: String?,
        basePrice: Double?,
        isActive: Boolean
    ): AppResult<AdminProduct> =
        dataSource.createProduct(AdminCreateProductRequest(categoryId, title, slug, description, basePrice, isActive)).map { it.toAdminDomain() }

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
        sizeId: Long,
        colorId: Long,
        sku: String,
        price: Double,
        compareAtPrice: Double?,
        isActive: Boolean,
        initialOnHand: Int
    ): AppResult<AdminVariant> =
        dataSource.createVariant(productId, AdminCreateVariantRequest(sizeId, colorId, sku, price, compareAtPrice, isActive, initialOnHand)).map { it.toAdminDomain() }

    override suspend fun updateVariant(
        variantId: Long,
        sku: String?,
        price: Double?,
        compareAtPrice: Double?,
        isActive: Boolean?
    ): AppResult<AdminVariant> =
        dataSource.updateVariant(variantId, AdminUpdateVariantRequest(sku, price, compareAtPrice, isActive)).map { it.toAdminDomain() }

    override suspend fun getInventory(variantId: Long): AppResult<AdminInventory> =
        dataSource.getInventory(variantId).map { it.toAdminDomain() }

    override suspend fun setInventory(variantId: Long, onHand: Int, version: Int?): AppResult<AdminInventory> =
        dataSource.setInventory(variantId, AdminInventorySetRequest(onHand, version)).map { it.toAdminDomain() }

    override suspend fun adjustInventory(variantId: Long, delta: Int, version: Int?): AppResult<AdminInventory> =
        dataSource.adjustInventory(variantId, AdminInventoryAdjustRequest(delta, version)).map { it.toAdminDomain() }
}
