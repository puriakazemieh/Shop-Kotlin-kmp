package com.kazemieh.network.order

import com.kazemieh.network.order.dto.request.AdminUpdateReturnRequestRequest
import com.kazemieh.network.order.dto.request.CreateReturnRequestRequest
import com.kazemieh.network.order.dto.response.AdminReturnRequestResponse
import com.kazemieh.network.order.dto.response.ReturnRequestResponse

interface ReturnRequestApi {
    suspend fun create(request: CreateReturnRequestRequest): ReturnRequestResponse
    suspend fun listMine(): List<ReturnRequestResponse>
    suspend fun adminList(): List<AdminReturnRequestResponse>
    suspend fun adminUpdateStatus(id: Long, request: AdminUpdateReturnRequestRequest): AdminReturnRequestResponse
}
