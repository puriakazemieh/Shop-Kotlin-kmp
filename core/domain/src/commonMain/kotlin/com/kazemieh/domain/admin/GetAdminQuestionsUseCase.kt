package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult

class GetAdminQuestionsUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(
        productId: Long? = null,
        isNew: Boolean? = null,
        page: Int = 0,
        size: Int = 20
    ): AppResult<AdminPage<AdminInteraction>> {
        return repository.listQuestions(productId, isNew, page, size)
    }
}
