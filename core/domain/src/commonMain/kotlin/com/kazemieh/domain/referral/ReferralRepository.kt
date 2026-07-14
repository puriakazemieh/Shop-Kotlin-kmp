package com.kazemieh.domain.referral

import com.kazemieh.common.AppResult

interface ReferralRepository {
    suspend fun getMyInfo(): AppResult<ReferralInfo>
}

class GetMyReferralInfoUseCase(private val repository: ReferralRepository) {
    suspend operator fun invoke(): AppResult<ReferralInfo> = repository.getMyInfo()
}
