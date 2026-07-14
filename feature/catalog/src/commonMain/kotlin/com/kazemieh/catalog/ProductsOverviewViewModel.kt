package com.kazemieh.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.catalog.GetActiveCampaignUseCase
import com.kazemieh.domain.catalog.GetBannersUseCase
import com.kazemieh.domain.catalog.GetCategoriesUseCase
import com.kazemieh.domain.catalog.GetProductsUseCase
import com.kazemieh.domain.catalog.ProductSummary
import com.kazemieh.domain.auth.IsUserLoggedInUseCase
import com.kazemieh.domain.blog.usecase.GetBlogsUseCase
import com.kazemieh.domain.favorite.ObserveFavoriteIdsUseCase
import com.kazemieh.domain.recentlyviewed.GetRecentlyViewedUseCase
import com.kazemieh.domain.favorite.ToggleFavoriteUseCase
import com.kazemieh.domain.story.GetStoriesUseCase
import com.kazemieh.domain.story.MarkStoryAsSeenUseCase
import com.kazemieh.domain.academy.GetCoursesUseCase
import com.kazemieh.domain.clinic.GetTherapistsUseCase
import com.kazemieh.domain.psychtest.GetPsychTestsUseCase
import com.kazemieh.designsystem.brand.BrandConfig
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductsOverviewViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val getActiveCampaignUseCase: GetActiveCampaignUseCase,
    private val getBannersUseCase: GetBannersUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val observeFavoriteIdsUseCase: ObserveFavoriteIdsUseCase,
    private val getStoriesUseCase: GetStoriesUseCase,
    private val markStoryAsSeenUseCase: MarkStoryAsSeenUseCase,
    private val getBlogsUseCase: GetBlogsUseCase,
    private val getRecentlyViewedUseCase: GetRecentlyViewedUseCase,
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase,
    private val getCoursesUseCase: GetCoursesUseCase,
    private val getTherapistsUseCase: GetTherapistsUseCase,
    private val getPsychTestsUseCase: GetPsychTestsUseCase,
    private val brandConfig: BrandConfig
) : ViewModel() {

    private val _state = MutableStateFlow(ProductsOverviewState())
    val state = _state.asStateFlow()

    private val _effect = Channel<ProductsOverviewEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        handleIntent(ProductsOverviewIntent.LoadProducts)
        loadStories()
        loadCategories()
        loadCampaign()
        loadBanners()
        loadBlogPosts()
        observeRecentlyViewed()
        observeFavorites()
        loadVerticalPreviews()
    }

    /** پیش‌نمایشِ چندتایی از دوره/درمانگر/تست روان‌شناسی — فقط اگر برند همان عمودی را روشن کرده باشد. */
    private fun loadVerticalPreviews() {
        if (brandConfig.features.academy) {
            viewModelScope.launch {
                when (val result = getCoursesUseCase()) {
                    is AppResult.Success -> _state.update { it.copy(courses = result.data.take(8)) }
                    else -> { /* بدون دوره؛ بخش نمایش داده نمی‌شود */ }
                }
            }
        }
        if (brandConfig.features.clinic) {
            viewModelScope.launch {
                when (val result = getTherapistsUseCase()) {
                    is AppResult.Success -> _state.update { it.copy(therapists = result.data.take(8)) }
                    else -> { /* بدون درمانگر؛ بخش نمایش داده نمی‌شود */ }
                }
            }
        }
        if (brandConfig.features.psychTests) {
            viewModelScope.launch {
                when (val result = getPsychTestsUseCase()) {
                    is AppResult.Success -> _state.update { it.copy(psychTests = result.data.take(8)) }
                    else -> { /* بدون تست؛ بخش نمایش داده نمی‌شود */ }
                }
            }
        }
    }

    private fun observeRecentlyViewed() {
        viewModelScope.launch {
            getRecentlyViewedUseCase().collectLatest { items ->
                _state.update { it.copy(recentlyViewed = items) }
            }
        }
    }

    private fun loadBlogPosts() {
        viewModelScope.launch {
            when (val result = getBlogsUseCase(page = 0, size = 6)) {
                is AppResult.Success -> _state.update { it.copy(blogPosts = result.data.content) }
                else -> { /* بدون مقاله؛ بخش نمایش داده نمی‌شود */ }
            }
        }
    }

    private fun loadCampaign() {
        viewModelScope.launch {
            when (val result = getActiveCampaignUseCase()) {
                is AppResult.Success -> _state.update { it.copy(campaign = result.data) }
                else -> { /* بدون کمپینِ فعال؛ بخش نمایش داده نمی‌شود */ }
            }
        }
    }

    private fun loadBanners() {
        viewModelScope.launch {
            when (val result = getBannersUseCase()) {
                is AppResult.Success -> _state.update { it.copy(banners = result.data) }
                else -> { /* بدون بنر؛ بخش نمایش داده نمی‌شود */ }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            observeFavoriteIdsUseCase().collectLatest { ids ->
                _state.update { state ->
                    state.copy(
                        products = state.products.map { product ->
                            product.copy(isFavorite = ids.contains(product.id))
                        }
                    )
                }
            }
        }
    }

    fun handleIntent(intent: ProductsOverviewIntent) {
        when (intent) {
            is ProductsOverviewIntent.LoadProducts -> {
                loadProducts()
                loadCategories()
            }
            is ProductsOverviewIntent.Refresh -> refresh()
            is ProductsOverviewIntent.OnProductClick -> {
                viewModelScope.launch {
                    _effect.send(ProductsOverviewEffect.NavigateToDetails(intent.slug))
                }
            }
            is ProductsOverviewIntent.OnCategoryClick -> {
                viewModelScope.launch {
                    _effect.send(ProductsOverviewEffect.NavigateToCategory(intent.id, intent.name))
                }
            }
            is ProductsOverviewIntent.OnFavoriteClick -> toggleFavorite(intent.product)
            is ProductsOverviewIntent.OnStoryClick -> {
                viewModelScope.launch {
                    _effect.send(ProductsOverviewEffect.NavigateToStory(intent.index))
                }
            }
            is ProductsOverviewIntent.OnStorySeen -> {
                viewModelScope.launch {
                    markStoryAsSeenUseCase(intent.id)
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _state.update { it.copy(isCategoriesLoading = true) }
            when (val result = getCategoriesUseCase()) {
                is AppResult.Success<*> -> {
                    _state.update { it.copy(categories = result.data as List<com.kazemieh.domain.catalog.Category>, isCategoriesLoading = false) }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isCategoriesLoading = false) }
                }
                is AppResult.Loading -> {
                    _state.update { it.copy(isCategoriesLoading = true) }
                }
            }
        }
    }

    private fun loadStories() {
        viewModelScope.launch {
            getStoriesUseCase().collectLatest { result ->
                when (result) {
                    is AppResult.Success -> {
                        _state.update { it.copy(stories = result.data, isStoriesLoading = false) }
                    }
                    is AppResult.Error -> {
                        _state.update { it.copy(isStoriesLoading = false) }
                    }
                    is AppResult.Loading -> {
                        _state.update { it.copy(isStoriesLoading = true) }
                    }
                }
            }
        }
    }

    private fun toggleFavorite(product: ProductSummary) {
        viewModelScope.launch {
            val isLoggedIn = isUserLoggedInUseCase().first()
            if (!isLoggedIn) {
                _effect.send(ProductsOverviewEffect.NavigateToAuth)
                return@launch
            }

            val isAdding = !product.isFavorite
            updateProductFavoriteState(product.id, isAdding)

            val result = toggleFavoriteUseCase(product.id, isAdding)

            if (result is AppResult.Error) {
                updateProductFavoriteState(product.id, !isAdding)
            }
        }
    }

    private fun updateProductFavoriteState(productId: Long, isFavorite: Boolean) {
        _state.update { currentState ->
            currentState.copy(
                products = currentState.products.map {
                    if (it.id == productId) it.copy(isFavorite = isFavorite) else it
                }
            )
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getProducts()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            getProducts()
            loadStories()
            loadCategories()
            loadCampaign()
            loadBanners()
            loadBlogPosts()
            loadVerticalPreviews()
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private suspend fun getProducts() {
        when (val result = getProductsUseCase(page = 0, size = 50)) {
            is AppResult.Success -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        products = result.data.items,
                        error = null
                    )
                }
            }
            is AppResult.Error -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            }
            is AppResult.Loading -> {
                _state.update { it.copy(isLoading = true) }
            }
        }
    }
}
