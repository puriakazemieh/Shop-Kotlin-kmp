package com.kazemieh.data.referral

import com.kazemieh.common.AppResult
import com.kazemieh.domain.referral.ReferralInfo
import com.kazemieh.domain.referral.ReferralRepository
import com.kazemieh.network.common.safeApiCall
import com.kazemieh.network.referral.ReferralApi

class ReferralRepositoryImpl(
    private val api: ReferralApi
) : ReferralRepository {
    override suspend fun getMyInfo(): AppResult<ReferralInfo> = safeApiCall {
        api.getMyInfo().let { ReferralInfo(code = it.code, referredCount = it.referredCount, totalEarned = it.totalEarned) }
    }
}
