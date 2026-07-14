package com.kazemieh.data.membership

import com.kazemieh.common.AppResult
import com.kazemieh.domain.membership.MembershipRepository
import com.kazemieh.domain.membership.MembershipStatus
import com.kazemieh.network.common.safeApiCall
import com.kazemieh.network.membership.MembershipApi
import com.kazemieh.network.membership.MembershipStatusResponse

class MembershipRepositoryImpl(
    private val api: MembershipApi
) : MembershipRepository {
    override suspend fun getMine(): AppResult<MembershipStatus> = safeApiCall { api.getMine().toDomain() }
    override suspend fun subscribe(): AppResult<MembershipStatus> = safeApiCall { api.subscribe().toDomain() }

    private fun MembershipStatusResponse.toDomain() = MembershipStatus(
        isActive = isActive, tier = tier, expiresAt = expiresAt,
        discountPercent = discountPercent, price = price
    )
}
