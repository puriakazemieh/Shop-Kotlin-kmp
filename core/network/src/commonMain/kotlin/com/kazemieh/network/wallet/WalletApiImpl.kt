package com.kazemieh.network.wallet

import com.kazemieh.network.common.PageResponse
import com.kazemieh.network.common.safeApiCallRaw
import com.kazemieh.network.wallet.dto.request.TopUpRequest
import com.kazemieh.network.wallet.dto.request.WithdrawRequest
import com.kazemieh.network.wallet.dto.response.WalletBalanceResponse
import com.kazemieh.network.wallet.dto.response.WalletTransactionResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class WalletApiImpl(private val client: HttpClient) : WalletApi {

    override suspend fun getBalance(): WalletBalanceResponse = safeApiCallRaw {
        client.get("api/wallet/balance")
    }

    override suspend fun getTransactions(page: Int, size: Int): PageResponse<WalletTransactionResponse> = safeApiCallRaw {
        client.get("api/wallet/transactions") {
            parameter("page", page)
            parameter("size", size)
        }
    }

    override suspend fun topUp(request: TopUpRequest): String = safeApiCallRaw {
        client.post("api/wallet/top-up") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun withdraw(request: WithdrawRequest) = safeApiCallRaw<Unit> {
        client.post("api/wallet/withdraw") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
