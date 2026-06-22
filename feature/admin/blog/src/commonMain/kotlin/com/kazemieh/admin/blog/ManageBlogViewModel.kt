package com.kazemieh.admin.blog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.blog.Blog
import com.kazemieh.domain.blog.BlogBlock
import com.kazemieh.domain.blog.usecase.GetBlogCategoriesUseCase
import com.kazemieh.domain.blog.usecase.admin.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ManageBlogViewModel(
    private val getAdminBlogDetailUseCase: GetAdminBlogDetailUseCase,
    private val createBlogUseCase: CreateBlogUseCase,
    private val updateBlogUseCase: UpdateBlogUseCase,
    private val getBlogCategoriesUseCase: GetBlogCategoriesUseCase,
    private val uploadBlogMediaUseCase: UploadBlogMediaUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ManageBlogState())
    val state: StateFlow<ManageBlogState> = _state.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = getBlogCategoriesUseCase()) {
                is AppResult.Success -> {
                    _state.update { it.copy(categories = result.data) }
                }
                else -> {}
            }
        }
    }

    fun loadBlog(slug: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = getAdminBlogDetailUseCase(slug)) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            blog = result.data,
                            contentBlocks = result.data.content ?: emptyList()
                        )
                    }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun addBlock(block: BlogBlock) {
        _state.update { it.copy(contentBlocks = it.contentBlocks + block) }
    }

    fun updateBlock(index: Int, block: BlogBlock) {
        _state.update {
            val newList = it.contentBlocks.toMutableList()
            newList[index] = block
            it.copy(contentBlocks = newList)
        }
    }

    fun removeBlock(index: Int) {
        _state.update {
            val newList = it.contentBlocks.toMutableList()
            newList.removeAt(index)
            it.copy(contentBlocks = newList)
        }
    }

    fun moveBlock(index: Int, up: Boolean) {
        _state.update {
            val newList = it.contentBlocks.toMutableList()
            val targetIndex = if (up) index - 1 else index + 1
            if (targetIndex in newList.indices) {
                val item = newList.removeAt(index)
                newList.add(targetIndex, item)
            }
            it.copy(contentBlocks = newList)
        }
    }

    fun uploadBlockImage(index: Int, bytes: ByteArray) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val result = uploadBlogMediaUseCase(bytes, "blog_image_${index}.png")
            when (result) {
                is AppResult.Success -> {
                    updateBlock(index, BlogBlock.Image(result.data))
                    _state.update { it.copy(isSaving = false) }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isSaving = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun saveBlog(
        id: Long,
        title: String,
        slug: String,
        summary: String,
        thumbnailUrl: String,
        status: String,
        isFeatured: Boolean,
        metaTitle: String,
        metaDescription: String,
        categoryId: Long?
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val blog = Blog(
                id = id,
                title = title,
                slug = slug,
                summary = summary,
                content = _state.value.contentBlocks,
                thumbnailUrl = thumbnailUrl,
                status = status,
                category = _state.value.categories.find { it.id == categoryId },
                isFeatured = isFeatured,
                metaTitle = metaTitle,
                metaDescription = metaDescription,
                viewCount = _state.value.blog?.viewCount ?: 0,
                readingTimeMinutes = _state.value.blog?.readingTimeMinutes ?: 5,
                createdAt = _state.value.blog?.createdAt ?: ""
            )

            val result = if (blog.id == 0L) {
                createBlogUseCase(blog)
            } else {
                updateBlogUseCase(blog.id, blog)
            }

            when (result) {
                is AppResult.Success -> {
                    _state.update { it.copy(isSaving = false, isSuccess = true) }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isSaving = false, error = result.message) }
                }
                else -> {}
            }
        }
    }
}

data class ManageBlogState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val blog: Blog? = null,
    val contentBlocks: List<BlogBlock> = emptyList(),
    val categories: List<com.kazemieh.domain.blog.BlogCategory> = emptyList(),
    val error: Any? = null
)
