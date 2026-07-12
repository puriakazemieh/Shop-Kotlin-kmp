package com.kazemieh.network.referral

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

@Serializable
data class ReferralInfoResponse(
    val code: String,
    val referredCount: Long,
    val totalEarned: Double
)

interface ReferralApi {
    suspend fun getMyInfo(): ReferralInfoResponse
}

class ReferralApiImpl(private val client: HttpClient) : ReferralApi {
    override suspend fun getMyInfo(): ReferralInfoResponse =
        com.kazemieh.network.common.safeApiCallRaw { client.get("api/referrals/mine") }
}
