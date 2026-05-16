package com.kazemieh.admin_panel.manage_product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Category
import com.kazemieh.domain.model.admin.AdminVariant
import com.kazemieh.domain.repository.Color
import com.kazemieh.domain.repository.Size
import com.kazemieh.domain.usecase.admin.CreateAdminProductUseCase
import com.kazemieh.domain.usecase.admin.CreateProductVariantUseCase
import com.kazemieh.domain.usecase.admin.DeleteAdminProductUseCase
import com.kazemieh.domain.usecase.admin.GetAdminProductDetailUseCase
import com.kazemieh.domain.usecase.admin.UpdateAdminProductUseCase
import com.kazemieh.domain.usecase.catalog.GetCategoriesUseCase
import com.kazemieh.domain.usecase.catalog.GetColorsUseCase
import com.kazemieh.domain.usecase.catalog.GetSizesUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ManageProductViewModel(
    private val getAdminProductDetailUseCase: GetAdminProductDetailUseCase,
    private val createAdminProductUseCase: CreateAdminProductUseCase,
    private val updateAdminProductUseCase: UpdateAdminProductUseCase,
    private val deleteAdminProductUseCase: DeleteAdminProductUseCase,
    private val createProductVariantUseCase: CreateProductVariantUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getSizesUseCase: GetSizesUseCase,
    private val getColorsUseCase: GetColorsUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId = savedStateHandle.get<Long>("id") ?: -1L

    private val _state = MutableStateFlow(ManageProductState())
    val state = _state.asStateFlow()

    private val _event = Channel<ManageProductUiEvent>()
    val event = _event.receiveAsFlow()

    init {
        loadInitialData()
        if (productId != -1L) {
            loadProductDetail()
        }
    }

    fun handleIntent(intent: ManageProductIntent) {
        when (intent) {
            is ManageProductIntent.UpdateTitle -> _state.update { it.copy(title = intent.title) }
            is ManageProductIntent.UpdateDescription -> _state.update { it.copy(description = intent.description) }
            is ManageProductIntent.UpdateBasePrice -> _state.update { it.copy(basePrice = intent.price) }
            is ManageProductIntent.UpdateIsActive -> _state.update { it.copy(isActive = intent.isActive) }
            is ManageProductIntent.UpdateCategory -> _state.update { it.copy(selectedCategory = intent.category) }
            is ManageProductIntent.SaveProduct -> saveProduct()
            is ManageProductIntent.DeleteProduct -> deleteProduct()
            is ManageProductIntent.AddImage -> addImage(intent.url)
            is ManageProductIntent.DeleteImage -> deleteImage(intent.imageId)
            is ManageProductIntent.AddVariant -> addVariant(intent)
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val categories = getCategoriesUseCase()
            val sizes = getSizesUseCase()
            val colors = getColorsUseCase()
            _state.update {
                it.copy(
                    categories = (categories as? AppResult.Success)?.data ?: emptyList(),
                    sizes = (sizes as? AppResult.Success)?.data ?: emptyList(),
                    colors = (colors as? AppResult.Success)?.data ?: emptyList()
                )
            }
        }
    }

    private fun loadProductDetail() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = getAdminProductDetailUseCase(productId)) {
                is AppResult.Success -> {
                    val detail = result.data
                    _state.update {
                        it.copy(
                            isLoading = false,
                            title = detail.product.title,
                            description = detail.product.description ?: "",
                            basePrice = detail.product.basePrice ?: 0.0,
                            isActive = detail.product.isActive,
                            selectedCategory = it.categories.find { c -> c.id == detail.product.categoryId },
                            images = detail.images.map { img ->
                                ProductImageUiModel(
                                    img.id,
                                    img.url
                                )
                            },
                            variants = detail.variants
                        )
                    }
                }

                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _event.send(ManageProductUiEvent.ShowError(result.message))
                }

                is AppResult.Loading -> {}
            }
        }
    }

    private fun saveProduct() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val currentState = _state.value
            val result = if (productId == -1L) {
                createAdminProductUseCase(
                    categoryId = currentState.selectedCategory?.id,
                    title = currentState.title,
                    slug = currentState.title.lowercase().replace(" ", "-"),
                    description = currentState.description,
                    basePrice = currentState.basePrice,
                    isActive = currentState.isActive
                )
            } else {
                updateAdminProductUseCase(
                    id = productId,
                    categoryId = currentState.selectedCategory?.id,
                    title = currentState.title,
                    slug = currentState.title.lowercase().replace(" ", "-"),
                    description = currentState.description,
                    basePrice = currentState.basePrice,
                    isActive = currentState.isActive
                )
            }

            when (result) {
                is AppResult.Success -> {
                    _event.send(ManageProductUiEvent.ShowSuccess("Product saved successfully"))
                    _event.send(ManageProductUiEvent.NavigateBack)
                }

                is AppResult.Error -> {
                    _event.send(ManageProductUiEvent.ShowError(result.message))
                }

                is AppResult.Loading -> {}
            }
            _state.update { it.copy(isSaving = false) }
        }
    }

    private fun deleteProduct() {
        viewModelScope.launch {
            when (val result = deleteAdminProductUseCase(productId)) {
                is AppResult.Success -> {
                    _event.send(ManageProductUiEvent.ShowSuccess("Product deleted successfully"))
                    _event.send(ManageProductUiEvent.NavigateBack)
                }

                is AppResult.Error -> {
                    _event.send(ManageProductUiEvent.ShowError(result.message))
                }

                is AppResult.Loading -> {}
            }
        }
    }

    private fun addImage(url: String) {
        // Implementation for adding image
    }

    private fun deleteImage(imageId: Long) {
        // Implementation for deleting image
    }

    private fun addVariant(intent: ManageProductIntent.AddVariant) {
        if (productId == -1L) {
            viewModelScope.launch {
                _event.send(ManageProductUiEvent.ShowError("Please save the product first before adding variants"))
            }
            return
        }
        viewModelScope.launch {
            val result = createProductVariantUseCase(
                productId = productId,
                sizeId = intent.sizeId,
                colorId = intent.colorId,
                sku = intent.sku,
                price = intent.price,
                compareAtPrice = null,
                isActive = true,
                initialOnHand = intent.initialOnHand
            )
            when (result) {
                is AppResult.Success -> {
                    _event.send(ManageProductUiEvent.ShowSuccess("Variant added"))
                    loadProductDetail()
                }

                is AppResult.Error -> _event.send(ManageProductUiEvent.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }
}

data class ManageProductState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val title: String = "",
    val description: String = "",
    val basePrice: Double = 0.0,
    val isActive: Boolean = true,
    val selectedCategory: Category? = null,
    val categories: List<Category> = emptyList(),
    val sizes: List<Size> = emptyList(),
    val colors: List<Color> = emptyList(),
    val images: List<ProductImageUiModel> = emptyList(),
    val variants: List<AdminVariant> = emptyList()
)

data class ProductImageUiModel(
    val id: Long,
    val url: String
)

sealed interface ManageProductIntent {
    data class UpdateTitle(val title: String) : ManageProductIntent
    data class UpdateDescription(val description: String) : ManageProductIntent
    data class UpdateBasePrice(val price: Double) : ManageProductIntent
    data class UpdateIsActive(val isActive: Boolean) : ManageProductIntent
    data class UpdateCategory(val category: Category) : ManageProductIntent
    data object SaveProduct : ManageProductIntent
    data object DeleteProduct : ManageProductIntent
    data class AddImage(val url: String) : ManageProductIntent
    data class DeleteImage(val imageId: Long) : ManageProductIntent
    data class AddVariant(
        val sizeId: Long,
        val colorId: Long,
        val sku: String,
        val price: Double,
        val initialOnHand: Int
    ) : ManageProductIntent
}

sealed class ManageProductUiEvent {
    data class ShowError(val message: String) : ManageProductUiEvent()
    data class ShowSuccess(val message: String) : ManageProductUiEvent()
    data object NavigateBack : ManageProductUiEvent()
}
