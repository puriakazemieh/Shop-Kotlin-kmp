package com.kazemieh.network.admin.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class DailySalesResponse(
    val date: String,
    val total: Double
)

@Serializable
data class VerticalCountsResponse(
    val courses: Long = 0,
    val therapists: Long = 0,
    val psychTests: Long = 0
)

@Serializable
data class AdminStatsResponse(
    val totalRevenue: Double,
    val totalOrders: Long,
    val totalProducts: Long,
    val totalCustomers: Long,
    val weeklySales: List<DailySalesResponse> = emptyList(),
    val verticalCounts: VerticalCountsResponse = VerticalCountsResponse(),
    val newOrdersToday: Long = 0,
    val ordersTrendPercent: Int = 0,
    val salesToday: Double = 0.0,
    val salesTrendPercent: Int = 0,
    val lowStockCount: Long = 0
)
