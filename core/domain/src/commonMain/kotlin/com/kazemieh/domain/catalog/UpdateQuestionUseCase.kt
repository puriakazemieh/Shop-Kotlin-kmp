package com.kazemieh.domain.catalog

import com.kazemieh.common.AppResult

class UpdateQuestionUseCase(
    private val repository: InteractionRepository
) {
    suspend operator fun invoke(questionId: Long, content: String): AppResult<Question> {
        return repository.updateQuestion(questionId, content)
    }
}
