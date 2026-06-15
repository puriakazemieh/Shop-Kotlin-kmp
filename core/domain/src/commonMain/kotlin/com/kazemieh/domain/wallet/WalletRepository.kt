package com.kazemieh.domain.wallet

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminPage

interface WalletRepository {
    suspend fun getBalance(): AppResult<WalletBalance>
    suspend fun getTransactions(page: Int, size: Int): AppResult<AdminPage<WalletTransaction>>
    suspend fun topUp(amount: Double): AppResult<String>
    suspend fun withdraw(amount: Double, iban: String): AppResult<Unit>

    // Admin
    suspend fun searchWalletUsers(query: String): AppResult<List<AdminWalletUser>>
    suspend fun adjustWalletBalance(userId: Long, amount: Double, description: String?): AppResult<Unit>
    suspend fun listWithdrawals(status: String?): AppResult<List<AdminWithdrawal>>
    suspend fun processWithdrawal(id: Long, status: String, adminNote: String?): AppResult<Unit>
}
