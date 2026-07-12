package com.kazemieh.network.membership

import com.kazemieh.network.common.safeApiCallRaw
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.serialization.Serializable

@Serializable
data class MembershipStatusResponse(
    val isActive: Boolean,
    val tier: String? = null,
    val expiresAt: String? = null,
    val discountPercent: Double,
    val price: Double
)

interface MembershipApi {
    suspend fun getMine(): MembershipStatusResponse
    suspend fun subscribe(): MembershipStatusResponse
}

class MembershipApiImpl(private val client: HttpClient) : MembershipApi {
    override suspend fun getMine(): MembershipStatusResponse = safeApiCallRaw {
        client.get("api/memberships/mine")
    }

    override suspend fun subscribe(): MembershipStatusResponse = safeApiCallRaw {
        client.post("api/memberships/subscribe")
    }
}
