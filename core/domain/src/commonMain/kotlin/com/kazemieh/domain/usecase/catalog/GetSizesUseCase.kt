package com.kazemieh.domain.usecase.catalog

import com.kazemieh.common.AppResult
import com.kazemieh.domain.repository.CatalogRepository
import com.kazemieh.domain.repository.Size

class GetSizesUseCase(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(): AppResult<List<Size>> {
        return repository.getSizes()
    }
}
