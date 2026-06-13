package com.kazemieh.domain.cart

import com.kazemieh.common.AppResult

class MoveToSaveForLaterUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(itemId: Long): AppResult<Cart> = repository.moveToSaveForLater(itemId)
}
