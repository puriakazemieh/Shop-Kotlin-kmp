package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminProductImage
import com.kazemieh.domain.admin.AdminRepository

class AddProductImageUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(productId: Long, bytes: ByteArray, sortOrder: Int?): AppResult<AdminProductImage> {
        return repository.addImage(productId, bytes, sortOrder)
    }
}
