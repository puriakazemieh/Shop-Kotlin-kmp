package com.kazemieh.blog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.blog.usecase.GetBlogCategoriesUseCase
import com.kazemieh.domain.blog.usecase.GetBlogsUseCase
import com.kazemieh.domain.blog.usecase.GetFeaturedBlogsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BlogListViewModel(
    private val getBlogsUseCase: GetBlogsUseCase,
    private val getFeaturedBlogsUseCase: GetFeaturedBlogsUseCase,
    private val getBlogCategoriesUseCase: GetBlogCategoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BlogListState())
    val state: StateFlow<BlogListState> = _state.asStateFlow()

    private val _effect = Channel<BlogListEffect>()
    val effect: Flow<BlogListEffect> = _effect.receiveAsFlow()

    init {
        handleIntent(BlogListIntent.LoadBlogs)
        handleIntent(BlogListIntent.LoadFeaturedBlogs)
        handleIntent(BlogListIntent.LoadCategories)
    }

    fun handleIntent(intent: BlogListIntent) {
        when (intent) {
            is BlogListIntent.LoadBlogs -> loadBlogs()
            is BlogListIntent.LoadFeaturedBlogs -> loadFeaturedBlogs()
            is BlogListIntent.LoadCategories -> loadCategories()
            is BlogListIntent.SelectCategory -> {
                _state.update { it.copy(selectedCategoryId = intent.categoryId) }
                loadBlogs()
            }
            is BlogListIntent.Search -> {
                _state.update { it.copy(searchQuery = intent.query) }
                loadBlogs()
            }
            is BlogListIntent.Refresh -> {
                loadCategories()
                loadBlogs()
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

    private fun loadFeaturedBlogs() {
        viewModelScope.launch {
            when (val result = getFeaturedBlogsUseCase()) {
                is AppResult.Success -> {
                    _state.update { it.copy(featuredBlogs = result.data) }
                }
                else -> {}
            }
        }
    }

    private fun loadBlogs() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val currentState = _state.value
            when (val result = getBlogsUseCase(
                categoryId = currentState.selectedCategoryId,
                searchQuery = currentState.searchQuery
            )) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            blogs = result.data.content,
                            error = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _state.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                    _effect.send(BlogListEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
