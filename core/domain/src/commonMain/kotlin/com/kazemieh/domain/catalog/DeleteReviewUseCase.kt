package com.kazemieh.domain.catalog

import com.kazemieh.common.AppResult

class DeleteReviewUseCase(
    private val repository: InteractionRepository
) {
    suspend operator fun invoke(reviewId: Long): AppResult<Unit> {
        return repository.deleteReview(reviewId)
    }
}
