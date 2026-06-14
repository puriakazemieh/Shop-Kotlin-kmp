package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult

class DeleteProductVideoUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(productId: Long, videoId: Long): AppResult<Unit> {
        return repository.deleteVideo(productId, videoId)
    }
}
