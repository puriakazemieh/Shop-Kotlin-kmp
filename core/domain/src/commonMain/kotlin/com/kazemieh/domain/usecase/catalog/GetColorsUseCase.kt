package com.kazemieh.domain.usecase.catalog

import com.kazemieh.common.AppResult
import com.kazemieh.domain.repository.CatalogRepository
import com.kazemieh.domain.repository.Color

class GetColorsUseCase(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(): AppResult<List<Color>> {
        return repository.getColors()
    }
}
