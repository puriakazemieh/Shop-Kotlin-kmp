package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult

class GetAdminStatsUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(): AppResult<AdminStats> = repository.getStats()
}
