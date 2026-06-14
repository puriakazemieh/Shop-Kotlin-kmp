package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult

class AddProductVideoUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(productId: Long, bytes: ByteArray, sortOrder: Int?): AppResult<AdminProductVideo> {
        return repository.addVideo(productId, bytes, sortOrder)
    }
}
