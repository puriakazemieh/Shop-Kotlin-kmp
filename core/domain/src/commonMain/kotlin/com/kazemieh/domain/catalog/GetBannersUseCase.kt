package com.kazemieh.domain.catalog

import com.kazemieh.common.AppResult

class GetBannersUseCase(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(): AppResult<List<Banner>> = repository.getBanners()
}
