package com.kazemieh.domain.catalog

data class Campaign(
    val id: Long,
    val title: String,
    val endsAt: String,
    val remainingSeconds: Long,
    val products: List<ProductSummary>
)
