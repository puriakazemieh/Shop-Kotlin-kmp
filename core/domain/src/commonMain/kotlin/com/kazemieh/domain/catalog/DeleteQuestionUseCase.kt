package com.kazemieh.domain.catalog

import com.kazemieh.common.AppResult

class DeleteQuestionUseCase(
    private val repository: InteractionRepository
) {
    suspend operator fun invoke(questionId: Long): AppResult<Unit> {
        return repository.deleteQuestion(questionId)
    }
}
