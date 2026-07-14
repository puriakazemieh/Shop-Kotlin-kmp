package com.kazemieh.domain.membership

import com.kazemieh.common.AppResult

data class MembershipStatus(
    val isActive: Boolean,
    val tier: String?,
    val expiresAt: String?,
    val discountPercent: Double,
    val price: Double
)

interface MembershipRepository {
    suspend fun getMine(): AppResult<MembershipStatus>
    suspend fun subscribe(): AppResult<MembershipStatus>
}

class GetMembershipStatusUseCase(private val repository: MembershipRepository) {
    suspend operator fun invoke(): AppResult<MembershipStatus> = repository.getMine()
}

class SubscribeMembershipUseCase(private val repository: MembershipRepository) {
    suspend operator fun invoke(): AppResult<MembershipStatus> = repository.subscribe()
}
