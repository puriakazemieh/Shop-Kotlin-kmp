package com.kazemieh.network.order

import com.kazemieh.network.common.safeApiCallRaw
import com.kazemieh.network.order.dto.request.AdminUpdateReturnRequestRequest
import com.kazemieh.network.order.dto.request.CreateReturnRequestRequest
import com.kazemieh.network.order.dto.response.AdminReturnRequestResponse
import com.kazemieh.network.order.dto.response.ReturnRequestResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.*

class ReturnRequestApiImpl(
    private val client: HttpClient
) : ReturnRequestApi {

    override suspend fun create(request: CreateReturnRequestRequest): ReturnRequestResponse = safeApiCallRaw {
        client.post("api/return-requests") { setBody(request) }
    }

    override suspend fun listMine(): List<ReturnRequestResponse> = safeApiCallRaw {
        client.get("api/return-requests/mine")
    }

    override suspend fun adminList(): List<AdminReturnRequestResponse> = safeApiCallRaw {
        client.get("api/admin/return-requests")
    }

    override suspend fun adminUpdateStatus(id: Long, request: AdminUpdateReturnRequestRequest): AdminReturnRequestResponse = safeApiCallRaw {
        client.patch("api/admin/return-requests/$id") { setBody(request) }
    }
}
