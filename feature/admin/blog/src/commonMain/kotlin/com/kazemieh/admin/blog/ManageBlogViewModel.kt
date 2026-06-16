package com.kazemieh.admin.blog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.blog.Blog
import com.kazemieh.domain.blog.usecase.GetBlogCategoriesUseCase
import com.kazemieh.domain.blog.usecase.admin.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ManageBlogViewModel(
    private val getAdminBlogDetailUseCase: GetAdminBlogDetailUseCase,
    private val createBlogUseCase: CreateBlogUseCase,
    private val updateBlogUseCase: UpdateBlogUseCase,
    private val getBlogCategoriesUseCase: GetBlogCategoriesUseCase
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
                    _state.update { it.copy(isLoading = false, blog = result.data) }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun saveBlog(blog: Blog) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
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
    val categories: List<com.kazemieh.domain.blog.BlogCategory> = emptyList(),
    val error: Any? = null
)
