package com.kazemieh.domain.order

import com.kazemieh.common.AppResult

interface ReturnRequestRepository {
    suspend fun create(orderItemId: Long, type: String, reason: String): AppResult<ReturnRequest>
    suspend fun listMine(): AppResult<List<ReturnRequest>>
    suspend fun adminList(): AppResult<List<AdminReturnRequest>>
    suspend fun adminUpdateStatus(id: Long, status: String, adminNote: String?): AppResult<AdminReturnRequest>
}
