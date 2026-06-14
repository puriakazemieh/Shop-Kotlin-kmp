package com.kazemieh.domain.catalog

import com.kazemieh.common.AppResult

class PostQuestionUseCase(
    private val repository: InteractionRepository
) {
    suspend operator fun invoke(request: CreateQuestionRequest): AppResult<Question> {
        return repository.postQuestion(request)
    }
}
