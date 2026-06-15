package com.kazemieh.domain.wallet

data class WalletBalance(
    val balance: Double,
    val userId: Long
)

data class WalletTransaction(
    val id: Long,
    val amount: Double,
    val type: String,
    val description: String?,
    val referenceId: String?,
    val createdAt: String
)

data class AdminWalletUser(
    val userId: Long,
    val email: String,
    val fullName: String,
    val balance: Double
)

data class AdminWithdrawal(
    val id: Long,
    val userId: Long,
    val amount: Double,
    val iban: String,
    val status: String,
    val createdAt: String
)
