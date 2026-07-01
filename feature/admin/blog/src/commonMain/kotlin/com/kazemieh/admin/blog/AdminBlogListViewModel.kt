package com.kazemieh.admin.blog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.blog.BlogCategory
import com.kazemieh.domain.blog.usecase.GetBlogCategoriesUseCase
import com.kazemieh.domain.blog.usecase.admin.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminBlogListViewModel(
    private val getAdminBlogsUseCase: GetAdminBlogsUseCase,
    private val deleteBlogUseCase: DeleteBlogUseCase,
    private val getBlogCategoriesUseCase: GetBlogCategoriesUseCase,
    private val deleteBlogCategoryUseCase: DeleteBlogCategoryUseCase,
    private val createBlogCategoryUseCase: CreateBlogCategoryUseCase,
    private val updateBlogCategoryUseCase: UpdateBlogCategoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminBlogListState())
    val state: StateFlow<AdminBlogListState> = _state.asStateFlow()

    private val _effect = Channel<AdminBlogListEffect>()
    val effect: Flow<AdminBlogListEffect> = _effect.receiveAsFlow()

    init {
        handleIntent(AdminBlogListIntent.LoadBlogs)
        handleIntent(AdminBlogListIntent.LoadCategories)
    }

    fun handleIntent(intent: AdminBlogListIntent) {
        when (intent) {
            is AdminBlogListIntent.LoadBlogs -> loadBlogs()
            is AdminBlogListIntent.LoadCategories -> loadCategories()
            is AdminBlogListIntent.DeleteBlog -> deleteBlog(intent.id)
            is AdminBlogListIntent.DeleteCategory -> deleteCategory(intent.id)
            is AdminBlogListIntent.CreateCategory -> createCategory(intent.name, intent.description)
            is AdminBlogListIntent.UpdateCategory -> updateCategory(intent.id, intent.name, intent.description)
        }
    }

    private fun createCategory(name: String, description: String?) {
        viewModelScope.launch {
            val category = BlogCategory(id = 0, name = name, slug = name, description = description)
            when (val result = createBlogCategoryUseCase(category)) {
                is AppResult.Success -> {
                    _effect.send(AdminBlogListEffect.CategorySaved)
                    loadCategories()
                }
                is AppResult.Error -> _effect.send(AdminBlogListEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    private fun updateCategory(id: Long, name: String, description: String?) {
        viewModelScope.launch {
            val category = BlogCategory(id = id, name = name, slug = name, description = description)
            when (val result = updateBlogCategoryUseCase(id, category)) {
                is AppResult.Success -> {
                    _effect.send(AdminBlogListEffect.CategorySaved)
                    loadCategories()
                }
                is AppResult.Error -> _effect.send(AdminBlogListEffect.ShowError(result.message))
                else -> {}
            }
        }
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

    private fun loadBlogs() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = getAdminBlogsUseCase()) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(isLoading = false, blogs = result.data.content, error = null)
                    }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                    _effect.send(AdminBlogListEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    private fun deleteBlog(id: Long) {
        viewModelScope.launch {
            when (val result = deleteBlogUseCase(id)) {
                is AppResult.Success -> {
                    _effect.send(AdminBlogListEffect.BlogDeleted)
                    loadBlogs()
                }
                is AppResult.Error -> {
                    _effect.send(AdminBlogListEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    private fun deleteCategory(id: Long) {
        viewModelScope.launch {
            when (val result = deleteBlogCategoryUseCase(id)) {
                is AppResult.Success -> {
                    _effect.send(AdminBlogListEffect.CategoryDeleted)
                    loadCategories()
                }
                is AppResult.Error -> {
                    _effect.send(AdminBlogListEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
