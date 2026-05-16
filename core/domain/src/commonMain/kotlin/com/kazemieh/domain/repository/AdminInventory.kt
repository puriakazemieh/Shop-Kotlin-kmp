package com.kazemieh.domain.repository

data class AdminInventory(
    val variantId: Long,
    val onHand: Int,
    val reserved: Int,
    val version: Int
)
