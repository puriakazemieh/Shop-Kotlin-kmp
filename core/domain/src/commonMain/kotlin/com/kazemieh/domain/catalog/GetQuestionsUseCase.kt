package com.kazemieh.domain.catalog

import com.kazemieh.common.AppResult

class GetQuestionsUseCase(
    private val repository: InteractionRepository
) {
    suspend operator fun invoke(productId: Long): AppResult<List<Question>> {
        return repository.getQuestions(productId)
    }
}
