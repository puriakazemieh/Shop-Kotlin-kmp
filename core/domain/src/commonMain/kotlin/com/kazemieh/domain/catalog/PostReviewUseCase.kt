package com.kazemieh.domain.catalog

import com.kazemieh.common.AppResult

class PostReviewUseCase(
    private val repository: InteractionRepository
) {
    suspend operator fun invoke(request: CreateReviewRequest): AppResult<Review> {
        return repository.postReview(request)
    }
}
