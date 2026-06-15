package com.kazemieh.data.wallet.repository

import com.kazemieh.common.AppResult
import com.kazemieh.common.map
import com.kazemieh.data.admin.mapper.toAdminPage
import com.kazemieh.data.wallet.mapper.toDomain
import com.kazemieh.domain.admin.AdminPage
import com.kazemieh.domain.wallet.*
import com.kazemieh.network.admin.AdminApi
import com.kazemieh.network.admin.dto.request.AdminProcessWithdrawalRequest
import com.kazemieh.network.admin.dto.request.AdminWalletAdjustRequest
import com.kazemieh.network.common.safeApiCall
import com.kazemieh.network.wallet.WalletApi
import com.kazemieh.network.wallet.dto.request.TopUpRequest
import com.kazemieh.network.wallet.dto.request.WithdrawRequest

class WalletRepositoryImpl(
    private val walletApi: WalletApi,
    private val adminApi: AdminApi
) : WalletRepository {

    override suspend fun getBalance(): AppResult<WalletBalance> = safeApiCall {
        walletApi.getBalance().toDomain()
    }

    override suspend fun getTransactions(
        page: Int,
        size: Int
    ): AppResult<AdminPage<WalletTransaction>> = safeApiCall {
        walletApi.getTransactions(page, size).toAdminPage { it.toDomain() }
    }

    override suspend fun topUp(amount: Double): AppResult<String> = safeApiCall {
        walletApi.topUp(TopUpRequest(amount))
    }

    override suspend fun withdraw(amount: Double, iban: String): AppResult<Unit> = safeApiCall {
        walletApi.withdraw(WithdrawRequest(amount, iban))
    }

    override suspend fun searchWalletUsers(query: String): AppResult<List<AdminWalletUser>> = safeApiCall {
        adminApi.searchWalletUsers(query).map { it.toDomain() }
    }

    override suspend fun adjustWalletBalance(
        userId: Long,
        amount: Double,
        description: String?
    ): AppResult<Unit> = safeApiCall {
        adminApi.adjustWalletBalance(AdminWalletAdjustRequest(userId, amount, description))
    }

    override suspend fun listWithdrawals(status: String?): AppResult<List<AdminWithdrawal>> = safeApiCall {
        adminApi.listWithdrawals(status).map { it.toDomain() }
    }

    override suspend fun processWithdrawal(
        id: Long,
        status: String,
        adminNote: String?
    ): AppResult<Unit> = safeApiCall {
        adminApi.processWithdrawal(id, AdminProcessWithdrawalRequest(status, adminNote))
    }
}
