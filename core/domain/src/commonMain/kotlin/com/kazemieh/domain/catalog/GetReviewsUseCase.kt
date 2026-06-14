package com.kazemieh.domain.catalog

import com.kazemieh.common.AppResult

class GetReviewsUseCase(
    private val repository: InteractionRepository
) {
    suspend operator fun invoke(productId: Long): AppResult<List<Review>> {
        return repository.getReviews(productId)
    }
}
