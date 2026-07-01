package com.kazemieh.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.Resources
import com.kazemieh.domain.auth.IsUserLoggedInUseCase
import com.kazemieh.domain.cart.AddToCartUseCase
import com.kazemieh.domain.catalog.CreateQuestionRequest
import com.kazemieh.domain.catalog.CreateReviewRequest
import com.kazemieh.domain.catalog.DeleteQuestionUseCase
import com.kazemieh.domain.catalog.DeleteReviewUseCase
import com.kazemieh.domain.catalog.GetProductDetailUseCase
import com.kazemieh.domain.catalog.GetProductsUseCase
import com.kazemieh.domain.catalog.GetQuestionsUseCase
import com.kazemieh.domain.catalog.GetReviewsUseCase
import com.kazemieh.domain.catalog.PostQuestionUseCase
import com.kazemieh.domain.catalog.PostReviewUseCase
import com.kazemieh.domain.catalog.UpdateQuestionUseCase
import com.kazemieh.domain.catalog.UpdateReviewUseCase
import com.kazemieh.domain.catalog.ProductDetail
import com.kazemieh.domain.catalog.ProductSummary
import com.kazemieh.domain.favorite.ObserveFavoriteIdsUseCase
import com.kazemieh.domain.favorite.ToggleFavoriteUseCase
import com.kazemieh.domain.recentlyviewed.AddRecentlyViewedUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val getProductsUseCase: GetProductsUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase,
    private val getReviewsUseCase: GetReviewsUseCase,
    private val postReviewUseCase: PostReviewUseCase,
    private val updateReviewUseCase: UpdateReviewUseCase,
    private val deleteReviewUseCase: DeleteReviewUseCase,
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val postQuestionUseCase: PostQuestionUseCase,
    private val updateQuestionUseCase: UpdateQuestionUseCase,
    private val deleteQuestionUseCase: DeleteQuestionUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val observeFavoriteIdsUseCase: ObserveFavoriteIdsUseCase,
    private val addRecentlyViewedUseCase: AddRecentlyViewedUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DetailsState())
    val state: StateFlow<DetailsState> = _state.asStateFlow()

    private val _effect = Channel<DetailsEffect>()
    val effect: Flow<DetailsEffect> = _effect.receiveAsFlow()

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            observeFavoriteIdsUseCase().collectLatest { ids ->
                _state.update { state ->
                    state.product?.let { product ->
                        state.copy(product = product.copy(isFavorite = ids.contains(product.id)))
                    } ?: state
                }
            }
        }
    }

    fun handleIntent(intent: DetailsIntent) {
        when (intent) {
            is DetailsIntent.LoadProduct -> loadProduct(intent.slug)
            is DetailsIntent.UpdateQuantity -> {
                val newQty = intent.quantity
                if (newQty <= 0) {
                    updateQuantityInCart(0)
                    _state.update { it.copy(quantity = 1, isAddedToCart = false) }
                } else {
                    _state.update { it.copy(quantity = newQty) }
                    updateQuantityInCart(newQty)
                }
            }
            is DetailsIntent.SelectVariant -> {
                _state.update {
                    it.copy(
                        selectedVariant = intent.variant,
                        selectedOptions = intent.variant.options,
                        isAddedToCart = false,
                        quantity = 1,
                        isCounterMode = true
                    )
                }
            }
            is DetailsIntent.SelectOption -> {
                val product = _state.value.product ?: return
                val currentOptions = _state.value.selectedOptions.toMutableMap()
                
                // Update the changed option
                currentOptions[intent.key] = intent.value
                
                // Try to find an exact match
                var matchingVariant = product.variants.find { it.options == currentOptions }
                
                // If no exact match, find the first variant that contains the newly selected option
                if (matchingVariant == null) {
                    matchingVariant = product.variants.find { 
                        it.options[intent.key] == intent.value 
                    }
                }

                _state.update {
                    it.copy(
                        selectedOptions = matchingVariant?.options ?: currentOptions,
                        selectedVariant = matchingVariant,
                        isAddedToCart = false,
                        quantity = 1,
                        isCounterMode = true
                    )
                }
            }
            is DetailsIntent.AddToCart -> addToCart()
            is DetailsIntent.SetCounterMode -> {
                _state.update { it.copy(isCounterMode = intent.isCounterMode) }
            }
            is DetailsIntent.LoadInteractions -> loadInteractions(intent.productId)
            is DetailsIntent.AddReview -> addReview(intent.productId, intent.rating, intent.comment, intent.parentId)
            is DetailsIntent.UpdateReview -> updateReview(intent.reviewId, intent.productId, intent.rating, intent.comment)
            is DetailsIntent.DeleteReview -> deleteReview(intent.reviewId, intent.productId)
            is DetailsIntent.AddQuestion -> addQuestion(intent.productId, intent.content, intent.parentId)
            is DetailsIntent.UpdateQuestion -> updateQuestion(intent.questionId, intent.productId, intent.content)
            is DetailsIntent.DeleteQuestion -> deleteQuestion(intent.questionId, intent.productId)
            is DetailsIntent.ToggleFavorite -> toggleFavorite(intent.productId, intent.isFavorite)
        }
    }

    private fun toggleFavorite(productId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            val isLoggedIn = isUserLoggedInUseCase().first()
            if (!isLoggedIn) {
                _effect.send(DetailsEffect.NavigateToAuth)
                return@launch
            }

            // Optimistic UI update
            _state.update { state ->
                state.product?.let { product ->
                    if (product.id == productId) {
                        state.copy(product = product.copy(isFavorite = !isFavorite))
                    } else state
                } ?: state
            }

            when (val result = toggleFavoriteUseCase(productId, !isFavorite)) {
                is AppResult.Success -> {
                    // Success, state already updated optimistically
                }
                is AppResult.Error -> {
                    // Revert on error
                    _state.update { state ->
                        state.product?.let { product ->
                            if (product.id == productId) {
                                state.copy(product = product.copy(isFavorite = isFavorite))
                            } else state
                        } ?: state
                    }
                    _effect.send(DetailsEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    private fun loadProduct(slug: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = getProductDetailUseCase(slug)) {
                is AppResult.Success -> {
                    val product = result.data
                    val firstVariant = product.variants.firstOrNull()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            product = product,
                            selectedVariant = firstVariant,
                            selectedOptions = firstVariant?.options ?: emptyMap(),
                            error = null
                        )
                    }
                    loadInteractions(product.id)
                    product.categoryId?.let { loadSimilarProducts(it, product.id) }
                    recordRecentlyViewed(product)
                }
                is AppResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    _effect.send(DetailsEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    private fun recordRecentlyViewed(product: ProductDetail) {
        viewModelScope.launch {
            if (!isUserLoggedInUseCase().first()) return@launch
            val prices = product.variants.map { it.price }
            val discounted = product.variants.mapNotNull { it.discountedPrice }
            val summary = ProductSummary(
                id = product.id,
                title = product.title,
                slug = product.slug,
                thumbnailUrl = product.images.firstOrNull()?.url,
                minPrice = prices.minOrNull() ?: product.basePrice,
                maxPrice = prices.maxOrNull() ?: product.basePrice,
                minDiscountedPrice = discounted.minOrNull() ?: product.discountedPrice,
                maxDiscountedPrice = discounted.maxOrNull() ?: product.discountedPrice,
                inStock = product.variants.isEmpty() || product.variants.any { it.available > 0 },
                categoryId = product.categoryId,
                categoryName = product.categoryName,
                options = emptyMap()
            )
            addRecentlyViewedUseCase(summary)
        }
    }

    private fun loadSimilarProducts(categoryId: Long, excludeId: Long) {
        viewModelScope.launch {
            when (val result = getProductsUseCase(categoryId = categoryId, size = 12)) {
                is AppResult.Success -> {
                    val items = result.data.items.filter { it.id != excludeId }.take(10)
                    _state.update { it.copy(similarProducts = items) }
                }
                else -> {}
            }
        }
    }

    private fun loadInteractions(productId: Long) {
        viewModelScope.launch {
            // Load Reviews
            when (val result = getReviewsUseCase(productId)) {
                is AppResult.Success -> _state.update { it.copy(reviews = result.data) }
                else -> {}
            }
            // Load Questions
            when (val result = getQuestionsUseCase(productId)) {
                is AppResult.Success -> _state.update { it.copy(questions = result.data) }
                else -> {}
            }
        }
    }

    private fun addReview(productId: Long, rating: Int?, comment: String, parentId: Long?) {
        viewModelScope.launch {
            val isLoggedIn = isUserLoggedInUseCase().first()
            if (!isLoggedIn) {
                _effect.send(DetailsEffect.NavigateToAuth)
                return@launch
            }

            val request = CreateReviewRequest(productId, rating, comment, parentId)
            when (val result = postReviewUseCase(request)) {
                is AppResult.Success -> loadInteractions(productId)
                is AppResult.Error -> _effect.send(DetailsEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    private fun updateReview(reviewId: Long, productId: Long, rating: Int?, comment: String) {
        viewModelScope.launch {
            when (val result = updateReviewUseCase(reviewId, rating, comment)) {
                is AppResult.Success -> loadInteractions(productId)
                is AppResult.Error -> _effect.send(DetailsEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    private fun deleteReview(reviewId: Long, productId: Long) {
        viewModelScope.launch {
            when (val result = deleteReviewUseCase(reviewId)) {
                is AppResult.Success -> loadInteractions(productId)
                is AppResult.Error -> _effect.send(DetailsEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    private fun addQuestion(productId: Long, content: String, parentId: Long?) {
        viewModelScope.launch {
            val isLoggedIn = isUserLoggedInUseCase().first()
            if (!isLoggedIn) {
                _effect.send(DetailsEffect.NavigateToAuth)
                return@launch
            }

            val request = CreateQuestionRequest(productId, content, parentId)
            when (val result = postQuestionUseCase(request)) {
                is AppResult.Success -> loadInteractions(productId)
                is AppResult.Error -> _effect.send(DetailsEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    private fun updateQuestion(questionId: Long, productId: Long, content: String) {
        viewModelScope.launch {
            when (val result = updateQuestionUseCase(questionId, content)) {
                is AppResult.Success -> loadInteractions(productId)
                is AppResult.Error -> _effect.send(DetailsEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    private fun deleteQuestion(questionId: Long, productId: Long) {
        viewModelScope.launch {
            when (val result = deleteQuestionUseCase(questionId)) {
                is AppResult.Success -> loadInteractions(productId)
                is AppResult.Error -> _effect.send(DetailsEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    private fun addToCart() {
        val currentState = _state.value
        val product = currentState.product ?: return

        // A product is simple if it has no variants or just one variant without options
        val isSimpleProduct = product.variants.isEmpty() ||
                (product.variants.size == 1 && product.variants.first().options.isEmpty())

        val variantId = currentState.selectedVariant?.id
        val productId = if (isSimpleProduct) product.id else null
        val finalVariantId = if (isSimpleProduct) null else variantId

        if (!isSimpleProduct && finalVariantId == null) {
            viewModelScope.launch {
                _effect.send(DetailsEffect.ShowError(Resources.String.PleaseSelectVariant))
            }
            return
        }

        viewModelScope.launch {
            val isLoggedIn = isUserLoggedInUseCase().first()
            if (!isLoggedIn) {
                _effect.send(DetailsEffect.NavigateToAuth)
                return@launch
            }

            when (val result = addToCartUseCase(productId = productId, variantId = finalVariantId, qty = currentState.quantity)) {
                is AppResult.Success -> {
                    _state.update { it.copy(isAddedToCart = true) }
                    _effect.send(DetailsEffect.AddedToCart)
                }
                is AppResult.Error -> {
                    _effect.send(DetailsEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    private fun updateQuantityInCart(newQty: Int) {
        val currentState = _state.value
        if (!currentState.isAddedToCart) return
        val product = currentState.product ?: return

        val isSimpleProduct = product.variants.isEmpty() ||
                (product.variants.size == 1 && product.variants.first().options.isEmpty())

        val variantId = currentState.selectedVariant?.id
        val productId = if (isSimpleProduct) product.id else null
        val finalVariantId = if (isSimpleProduct) null else variantId

        viewModelScope.launch {
            // Using addToCart with a specific quantity effectively sets/updates it in our backend implementation
            addToCartUseCase(productId = productId, variantId = finalVariantId, qty = newQty)
        }
    }
}
