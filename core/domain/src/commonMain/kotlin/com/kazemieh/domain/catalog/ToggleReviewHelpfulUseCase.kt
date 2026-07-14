package com.kazemieh.domain.catalog

import com.kazemieh.common.AppResult

class ToggleReviewHelpfulUseCase(
    private val repository: InteractionRepository
) {
    suspend operator fun invoke(reviewId: Long): AppResult<Review> {
        return repository.toggleReviewHelpful(reviewId)
    }
}
