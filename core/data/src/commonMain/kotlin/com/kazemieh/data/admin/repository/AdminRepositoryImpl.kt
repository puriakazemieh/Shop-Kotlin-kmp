package com.kazemieh.data.admin.repository

import com.kazemieh.network.admin.dto.request.*
import com.kazemieh.network.admin.dto.response.*
import com.kazemieh.domain.admin.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*
import com.kazemieh.data.admin.mapper.*
import com.kazemieh.data.admin.source.AdminDataSource




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
        discountedPrice: Double?,
        sku: String?,
        initialOnHand: Int?,
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
                discountedPrice,
                sku,
                initialOnHand,
                isActive,
                variants?.map {
                    AdminCreateVariantRequest(
                        it.options.map { option -> AdminVariantOptionRequest(option.type, option.value) },
                        it.sku,
                        it.price,
                        it.discountedPrice,
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
        discountedPrice: Double?,
        isActive: Boolean?
    ): AppResult<AdminProduct> =
        dataSource.updateProduct(id, AdminUpdateProductRequest(categoryId, title, slug, description, basePrice, discountedPrice, isActive)).map { it.toAdminDomain() }

    override suspend fun deleteProduct(id: Long): AppResult<Unit> =
        dataSource.deleteProduct(id)

    override suspend fun addImage(productId: Long, bytes: ByteArray, sortOrder: Int?): AppResult<AdminProductImage> =
        dataSource.addImage(productId, bytes, sortOrder).map { it.toAdminDomain() }

    override suspend fun reorderImages(productId: Long, items: List<Pair<Long, Int>>): AppResult<List<AdminProductImage>> =
        dataSource.reorderImages(productId, AdminReorderImagesRequest(items.map { ReorderItem(it.first, it.second) })).map { list -> list.map { it.toAdminDomain() } }

    override suspend fun deleteImage(productId: Long, imageId: Long): AppResult<Unit> =
        dataSource.deleteImage(productId, imageId)

    override suspend fun addVideo(productId: Long, bytes: ByteArray, sortOrder: Int?): AppResult<AdminProductVideo> =
        dataSource.addVideo(productId, bytes, sortOrder).map { it.toAdminDomain() }

    override suspend fun deleteVideo(productId: Long, videoId: Long): AppResult<Unit> =
        dataSource.deleteVideo(productId, videoId)

    override suspend fun createVariant(
        productId: Long,
        options: List<AdminVariantOption>,
        sku: String,
        price: Double,
        discountedPrice: Double?,
        compareAtPrice: Double?,
        isActive: Boolean,
        initialOnHand: Int
    ): AppResult<AdminVariant> =
        dataSource.createVariant(
            productId,
            AdminCreateVariantRequest(
                options.map { AdminVariantOptionRequest(it.type, it.value) },
                sku,
                price,
                discountedPrice,
                compareAtPrice,
                isActive,
                initialOnHand
            )
        ).map { it.toAdminDomain() }

    override suspend fun updateVariant(
        variantId: Long,
        sku: String?,
        price: Double?,
        discountedPrice: Double?,
        compareAtPrice: Double?,
        options: List<AdminVariantOption>?,
        isActive: Boolean?
    ): AppResult<AdminVariant> =
        dataSource.updateVariant(
            variantId,
            AdminUpdateVariantRequest(
                sku,
                price,
                discountedPrice,
                compareAtPrice,
                options?.map { AdminVariantOptionRequest(it.type, it.value) },
                isActive
            )
        ).map { it.toAdminDomain() }

    override suspend fun deleteVariant(variantId: Long): AppResult<Unit> =
        dataSource.deleteVariant(variantId)

    override suspend fun listOptions(): AppResult<List<AdminOption>> =
        dataSource.listOptions().map { list -> list.map { it.toAdminDomain() } }

    override suspend fun createOptionType(name: String): AppResult<AdminOption> =
        dataSource.createOptionType(AdminOptionTypeRequest(name)).map { it.toAdminDomain() }

    override suspend fun updateOptionType(id: Long, name: String): AppResult<AdminOption> =
        dataSource.updateOptionType(id, AdminOptionTypeRequest(name)).map { it.toAdminDomain() }

    override suspend fun deleteOptionType(id: Long): AppResult<Unit> =
        dataSource.deleteOptionType(id)

    override suspend fun createOptionValue(optionTypeId: Long, value: String): AppResult<AdminOptionValue> =
        dataSource.createOptionValue(AdminOptionValueRequest(optionTypeId, value)).map { it.toAdminDomain() }

    override suspend fun updateOptionValue(id: Long, optionTypeId: Long, value: String): AppResult<AdminOptionValue> =
        dataSource.updateOptionValue(id, AdminOptionValueRequest(optionTypeId, value)).map { it.toAdminDomain() }

    override suspend fun deleteOptionValue(id: Long): AppResult<Unit> =
        dataSource.deleteOptionValue(id)

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

    override suspend fun createDiscount(param: CreateDiscountParam): AppResult<Discount> =
        dataSource.createDiscount(param.toRequest()).map { it.toAdminDomain() }

    override suspend fun listDiscounts(): AppResult<List<Discount>> =
        dataSource.listDiscounts().map { list -> list.map { it.toAdminDomain() } }

    override suspend fun updateDiscount(id: Long, param: UpdateDiscountParam): AppResult<Discount> =
        dataSource.updateDiscount(id, param.toRequest()).map { it.toAdminDomain() }

    override suspend fun deleteDiscount(id: Long): AppResult<Unit> =
        dataSource.deleteDiscount(id)

    override suspend fun listReviews(
        productId: Long?,
        isNew: Boolean?,
        page: Int,
        size: Int
    ): AppResult<AdminPage<AdminInteraction>> =
        dataSource.listReviews(productId, isNew, page, size).map { it.toAdminPage { dto -> dto.toAdminDomain() } }

    override suspend fun listQuestions(
        productId: Long?,
        isNew: Boolean?,
        page: Int,
        size: Int
    ): AppResult<AdminPage<AdminInteraction>> =
        dataSource.listQuestions(productId, isNew, page, size).map { it.toAdminPage { dto -> dto.toAdminDomain() } }
}
