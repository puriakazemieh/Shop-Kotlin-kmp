package com.kazemieh.domain.favorite

import kotlinx.coroutines.flow.Flow

class ObserveFavoriteIdsUseCase(private val repository: FavoriteRepository) {
    operator fun invoke(): Flow<Set<Long>> = repository.observeFavoriteIds()
}
