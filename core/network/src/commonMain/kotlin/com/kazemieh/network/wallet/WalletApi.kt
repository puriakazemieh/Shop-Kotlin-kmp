package com.kazemieh.network.wallet

import com.kazemieh.network.common.PageResponse
import com.kazemieh.network.wallet.dto.request.TopUpRequest
import com.kazemieh.network.wallet.dto.request.WithdrawRequest
import com.kazemieh.network.wallet.dto.response.WalletBalanceResponse
import com.kazemieh.network.wallet.dto.response.WalletTransactionResponse

interface WalletApi {
    suspend fun getBalance(): WalletBalanceResponse
    suspend fun getTransactions(page: Int, size: Int): PageResponse<WalletTransactionResponse>
    suspend fun topUp(request: TopUpRequest): String
    suspend fun withdraw(request: WithdrawRequest)
}
