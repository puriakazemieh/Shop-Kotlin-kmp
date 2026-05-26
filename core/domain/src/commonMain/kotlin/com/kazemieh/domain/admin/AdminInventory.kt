package com.kazemieh.domain.admin

data class AdminInventory(
    val variantId: Long,
    val onHand: Int,
    val reserved: Int,
    val version: Int
)
