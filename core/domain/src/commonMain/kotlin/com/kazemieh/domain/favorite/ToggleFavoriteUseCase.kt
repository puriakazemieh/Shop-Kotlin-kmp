package com.kazemieh.domain.favorite

import com.kazemieh.common.AppResult

class ToggleFavoriteUseCase(private val repository: FavoriteRepository) {
    suspend operator fun invoke(productId: Long, isFavorite: Boolean): AppResult<Unit> {
        return if (isFavorite) {
            repository.addToFavorites(productId)
        } else {
            repository.removeFromFavorites(productId)
        }
    }
}
