package com.kazemieh.blog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.blog.usecase.GetBlogDetailUseCase
import com.kazemieh.domain.blog.usecase.GetRelatedBlogsUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BlogDetailViewModel(
    private val getBlogDetailUseCase: GetBlogDetailUseCase,
    private val getRelatedBlogsUseCase: GetRelatedBlogsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BlogDetailState())
    val state: StateFlow<BlogDetailState> = _state.asStateFlow()

    private val _effect = Channel<BlogDetailEffect>()
    val effect: Flow<BlogDetailEffect> = _effect.receiveAsFlow()

    fun handleIntent(intent: BlogDetailIntent) {
        when (intent) {
            is BlogDetailIntent.LoadBlog -> loadBlog(intent.slug)
        }
    }

    private fun loadBlog(slug: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val blogDeferred = async { getBlogDetailUseCase(slug) }
            val relatedDeferred = async { getRelatedBlogsUseCase(slug) }

            val blogResult = blogDeferred.await()
            val relatedResult = relatedDeferred.await()

            _state.update { it.copy(isLoading = false) }

            if (blogResult is AppResult.Success) {
                _state.update {
                    it.copy(
                        blog = blogResult.data,
                        relatedBlogs = (relatedResult as? AppResult.Success)?.data ?: emptyList(),
                        error = null
                    )
                }
            } else if (blogResult is AppResult.Error) {
                _state.update { it.copy(error = blogResult.message) }
                _effect.send(BlogDetailEffect.ShowError(blogResult.message))
            }
        }
    }
}
