package com.kazemieh.domain.catalog

import com.kazemieh.common.AppResult

class UpdateReviewUseCase(
    private val repository: InteractionRepository
) {
    suspend operator fun invoke(reviewId: Long, rating: Int?, comment: String): AppResult<Review> {
        return repository.updateReview(reviewId, rating, comment)
    }
}
