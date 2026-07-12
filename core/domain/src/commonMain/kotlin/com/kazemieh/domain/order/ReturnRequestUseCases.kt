package com.kazemieh.domain.order

import com.kazemieh.common.AppResult

class CreateReturnRequestUseCase(private val repository: ReturnRequestRepository) {
    suspend operator fun invoke(orderItemId: Long, type: String, reason: String): AppResult<ReturnRequest> =
        repository.create(orderItemId, type, reason)
}

class ListMyReturnRequestsUseCase(private val repository: ReturnRequestRepository) {
    suspend operator fun invoke(): AppResult<List<ReturnRequest>> = repository.listMine()
}

class AdminListReturnRequestsUseCase(private val repository: ReturnRequestRepository) {
    suspend operator fun invoke(): AppResult<List<AdminReturnRequest>> = repository.adminList()
}

class AdminUpdateReturnRequestUseCase(private val repository: ReturnRequestRepository) {
    suspend operator fun invoke(id: Long, status: String, adminNote: String?): AppResult<AdminReturnRequest> =
        repository.adminUpdateStatus(id, status, adminNote)
}
