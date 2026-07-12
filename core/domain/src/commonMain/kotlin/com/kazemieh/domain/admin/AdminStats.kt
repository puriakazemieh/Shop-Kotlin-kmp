package com.kazemieh.domain.admin

data class DailySales(
    val date: String,
    val total: Double
)

data class VerticalCounts(
    val courses: Long = 0,
    val therapists: Long = 0,
    val psychTests: Long = 0
)

data class AdminStats(
    val totalRevenue: Double,
    val totalOrders: Long,
    val totalProducts: Long,
    val totalCustomers: Long,
    val weeklySales: List<DailySales>,
    val verticalCounts: VerticalCounts = VerticalCounts()
)
