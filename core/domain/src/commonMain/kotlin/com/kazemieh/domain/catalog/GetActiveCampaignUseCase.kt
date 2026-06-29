package com.kazemieh.domain.catalog

import com.kazemieh.common.AppResult

class GetActiveCampaignUseCase(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(): AppResult<Campaign?> = repository.getActiveCampaign()
}
