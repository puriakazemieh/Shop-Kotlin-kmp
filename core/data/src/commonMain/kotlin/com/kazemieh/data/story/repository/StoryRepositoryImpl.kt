package com.kazemieh.data.story.repository

import com.kazemieh.common.AppResult
import com.kazemieh.common.map
import com.kazemieh.data.story.mapper.toDomain
import com.kazemieh.data.story.source.StoryDataSource
import com.kazemieh.domain.story.Story
import com.kazemieh.domain.story.StoryRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

class StoryRepositoryImpl(
    private val dataSource: StoryDataSource,
    private val settings: Settings
) : StoryRepository {
    private val seenIdsKey = "seen_story_ids"

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getStories(): Flow<AppResult<List<Story>>> = refreshTrigger.flatMapLatest {
        flow {
            emit(AppResult.Loading)
            val seenIds = getSeenIds()
            emit(dataSource.getStories().map { list ->
                list.map { it.toDomain(seenIds.contains(it.id)) }
            })
        }
    }

    override suspend fun markAsSeen(id: Long) {
        val seenIds = getSeenIds().toMutableSet()
        if (seenIds.add(id)) {
            saveSeenIds(seenIds)
            refreshTrigger.emit(Unit)
        }
    }

    override suspend fun getAdminStories(): AppResult<List<Story>> {
        return dataSource.getAdminStories().map { list ->
            list.map { it.toDomain(false) }
        }
    }

    override suspend fun createStory(
        bytes: ByteArray,
        mediaType: com.kazemieh.domain.story.StoryMediaType,
        productId: Long?,
        title: String?
    ): AppResult<Story> {
        return dataSource.createStory(bytes, mediaType.name, productId, title).map { it.toDomain(false) }
    }

    override suspend fun updateStory(
        id: Long,
        productId: Long?,
        title: String?
    ): AppResult<Story> {
        return dataSource.updateStory(id, productId, title).map { it.toDomain(false) }
    }

    override suspend fun deleteStory(id: Long): AppResult<Unit> {
        return dataSource.deleteStory(id)
    }

    private fun getSeenIds(): Set<Long> {
        val raw = settings.getString(seenIdsKey, "")
        if (raw.isEmpty()) return emptySet()
        return raw.split(",").mapNotNull { it.toLongOrNull() }.toSet()
    }

    private fun saveSeenIds(ids: Set<Long>) {
        settings.putString(seenIdsKey, ids.joinToString(","))
    }
}
