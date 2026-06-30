package com.kazemieh.domain.admin

data class DailySales(
    val date: String,
    val total: Double
)

data class AdminStats(
    val totalRevenue: Double,
    val totalOrders: Long,
    val totalProducts: Long,
    val totalCustomers: Long,
    val weeklySales: List<DailySales>
)
