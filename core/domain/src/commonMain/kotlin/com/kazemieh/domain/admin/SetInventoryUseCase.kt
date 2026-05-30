package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminInventory
import com.kazemieh.domain.admin.AdminRepository

class SetInventoryUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(variantId: Long, onHand: Int): AppResult<AdminInventory> {
        return repository.setInventory(variantId, onHand, null)
    }
}
