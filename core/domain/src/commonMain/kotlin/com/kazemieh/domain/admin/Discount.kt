package com.kazemieh.domain.admin

enum class DiscountType {
    PERCENTAGE, FIXED_AMOUNT
}

data class Discount(
    val id: Long,
    val code: String,
    val type: DiscountType,
    val value: Double,
    val maxDiscountAmount: Double?,
    val minOrderAmount: Double?,
    val startDate: String?,
    val endDate: String?,
    val usageLimit: Int?,
    val usageCount: Int,
    val isActive: Boolean
)

data class CreateDiscountParam(
    val code: String,
    val type: DiscountType,
    val value: Double,
    val maxDiscountAmount: Double? = null,
    val minOrderAmount: Double? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val usageLimit: Int? = null,
    val isActive: Boolean = true
)
