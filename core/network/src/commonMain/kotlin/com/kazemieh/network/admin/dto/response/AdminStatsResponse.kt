package com.kazemieh.network.admin.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class DailySalesResponse(
    val date: String,
    val total: Double
)

@Serializable
data class AdminStatsResponse(
    val totalRevenue: Double,
    val totalOrders: Long,
    val totalProducts: Long,
    val totalCustomers: Long,
    val weeklySales: List<DailySalesResponse> = emptyList()
)
