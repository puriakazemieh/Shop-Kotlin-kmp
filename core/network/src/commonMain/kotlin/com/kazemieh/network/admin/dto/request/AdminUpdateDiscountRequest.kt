package com.kazemieh.network.admin.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminUpdateDiscountRequest(
    val code: String? = null,
    val type: String? = null,
    val value: Double? = null,
    val maxDiscountAmount: Double? = null,
    val minOrderAmount: Double? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val usageLimit: Int? = null,
    val isActive: Boolean? = null
)
