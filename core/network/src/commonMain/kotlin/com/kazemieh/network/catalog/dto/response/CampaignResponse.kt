package com.kazemieh.network.catalog.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class CampaignResponse(
    val id: Long = 0,
    val title: String = "",
    val endsAt: String = "",
    val remainingSeconds: Long = 0,
    val products: List<ProductSummaryResponse> = emptyList()
)
