package com.kazemieh.data.order.repository

import com.kazemieh.common.AppResult
import com.kazemieh.domain.order.AdminReturnRequest
import com.kazemieh.domain.order.ReturnRequest
import com.kazemieh.domain.order.ReturnRequestRepository
import com.kazemieh.network.common.safeApiCall
import com.kazemieh.network.order.ReturnRequestApi
import com.kazemieh.network.order.dto.request.AdminUpdateReturnRequestRequest
import com.kazemieh.network.order.dto.request.CreateReturnRequestRequest
import com.kazemieh.network.order.dto.response.AdminReturnRequestResponse
import com.kazemieh.network.order.dto.response.ReturnRequestResponse

class ReturnRequestRepositoryImpl(
    private val api: ReturnRequestApi
) : ReturnRequestRepository {

    override suspend fun create(orderItemId: Long, type: String, reason: String): AppResult<ReturnRequest> = safeApiCall {
        api.create(CreateReturnRequestRequest(orderItemId, type, reason)).toDomain()
    }

    override suspend fun listMine(): AppResult<List<ReturnRequest>> = safeApiCall {
        api.listMine().map { it.toDomain() }
    }

    override suspend fun adminList(): AppResult<List<AdminReturnRequest>> = safeApiCall {
        api.adminList().map { it.toAdminDomain() }
    }

    override suspend fun adminUpdateStatus(id: Long, status: String, adminNote: String?): AppResult<AdminReturnRequest> = safeApiCall {
        api.adminUpdateStatus(id, AdminUpdateReturnRequestRequest(status, adminNote)).toAdminDomain()
    }

    private fun ReturnRequestResponse.toDomain() = ReturnRequest(
        id = id, orderId = orderId, orderItemId = orderItemId, itemTitle = itemTitle,
        type = type, reason = reason, status = status, adminNote = adminNote,
        createdAt = createdAt, resolvedAt = resolvedAt
    )

    private fun AdminReturnRequestResponse.toAdminDomain() = AdminReturnRequest(
        id = id, orderId = orderId, orderItemId = orderItemId, itemTitle = itemTitle,
        userId = userId, userName = userName, type = type, reason = reason, status = status,
        adminNote = adminNote, createdAt = createdAt, resolvedAt = resolvedAt
    )
}
