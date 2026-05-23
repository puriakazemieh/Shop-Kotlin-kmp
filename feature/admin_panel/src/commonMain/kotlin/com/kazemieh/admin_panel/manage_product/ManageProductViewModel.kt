package com.kazemieh.admin_panel.manage_product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.Resources
import com.kazemieh.domain.model.Category
import com.kazemieh.domain.model.admin.AdminOption
import com.kazemieh.domain.model.admin.AdminVariant
import com.kazemieh.domain.usecase.admin.*
import com.kazemieh.domain.usecase.catalog.GetCategoriesUseCase
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
    private val updateProductVariantUseCase: UpdateProductVariantUseCase,
    private val deleteProductVariantUseCase: DeleteProductVariantUseCase,
    private val createAdminCategoryUseCase: CreateAdminCategoryUseCase,
    private val deleteAdminCategoryUseCase: DeleteAdminCategoryUseCase,
    private val addProductImageUseCase: AddProductImageUseCase,
    private val deleteProductImageUseCase: DeleteProductImageUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getAdminOptionsUseCase: GetAdminOptionsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productId = savedStateHandle.get<Long>("id") ?: -1L

    private val _state = MutableStateFlow(ManageProductState())
    val state = _state.asStateFlow()

    private val _event = Channel<ManageProductUiEvent>()
    val event = _event.receiveAsFlow()

    init {
        loadInitialData()
    }

    fun handleIntent(intent: ManageProductIntent) {
        when (intent) {
            is ManageProductIntent.UpdateTitle -> _state.update {
                it.copy(
                    title = intent.title,
                    slug = intent.title.lowercase().replace(" ", "-")
                )
            }

            is ManageProductIntent.UpdateDescription -> _state.update { it.copy(description = intent.description) }
            is ManageProductIntent.UpdateBasePrice -> _state.update { it.copy(basePrice = intent.price) }
            is ManageProductIntent.UpdateIsActive -> _state.update { it.copy(isActive = intent.isActive) }
            is ManageProductIntent.SelectCategory -> _state.update { it.copy(selectedCategory = intent.category) }
            is ManageProductIntent.SaveProduct -> saveProduct()
            is ManageProductIntent.DeleteProduct -> deleteProduct()
            is ManageProductIntent.DeleteImage -> deleteImage(intent.imageId)
            is ManageProductIntent.AddVariant -> addVariant(
                intent.optionType,
                intent.optionValue,
                intent.sku,
                intent.price,
                intent.initialOnHand
            )
            is ManageProductIntent.UpdateVariantInfo -> updateVariant(
                intent.id,
                intent.sku,
                intent.price,
                intent.optionType,
                intent.optionValue,
                intent.isActive
            )

            is ManageProductIntent.DeleteVariant -> deleteVariant(intent.variantId)
            is ManageProductIntent.CreateCategory -> createCategory(
                intent.name,
                intent.slug,
                intent.parentId
            )

            is ManageProductIntent.DeleteCategory -> deleteCategory(intent.id)
            is ManageProductIntent.UploadImage -> uploadImage(intent.bytes)
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val categoriesResult = getCategoriesUseCase()
            val optionsResult = getAdminOptionsUseCase()
            _state.update {
                it.copy(
                    categories = if (categoriesResult is AppResult.Success) categoriesResult.data else emptyList(),
                    availableOptions = if (optionsResult is AppResult.Success) optionsResult.data else emptyList()
                )
            }
            if (productId != -1L) {
                loadProductDetail()
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
                            slug = detail.product.slug,
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
        val currentState = _state.value
        if (productId == -1L && currentState.variants.isEmpty()) {
            viewModelScope.launch {
                _event.send(ManageProductUiEvent.ShowError(Resources.String.PleaseAddAtLeastOneVariant))
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val result = if (productId == -1L) {
                createAdminProductUseCase(
                    categoryId = currentState.selectedCategory?.id,
                    title = currentState.title,
                    slug = currentState.slug,
                    description = currentState.description,
                    basePrice = currentState.basePrice,
                    isActive = currentState.isActive,
                    variants = currentState.variants.map {
                        com.kazemieh.domain.model.admin.AdminCreateVariant(
                            optionType = it.options.keys.firstOrNull() ?: "",
                            optionValue = it.options.values.firstOrNull() ?: "",
                            sku = it.sku,
                            price = it.price,
                            compareAtPrice = it.compareAtPrice,
                            isActive = it.isActive,
                            initialOnHand = it.onHand
                        )
                    }
                )
            } else {
                updateAdminProductUseCase(
                    id = productId,
                    categoryId = currentState.selectedCategory?.id,
                    title = currentState.title,
                    slug = currentState.slug,
                    description = currentState.description,
                    basePrice = currentState.basePrice,
                    isActive = currentState.isActive
                )
            }

            when (result) {
                is AppResult.Success<*> -> {
                    val newId = (result.data as? com.kazemieh.domain.model.admin.AdminProduct)?.id
                    if (productId == -1L && newId != null) {
                        currentState.selectedImageBytes.forEach { bytes ->
                            addProductImageUseCase(newId, bytes, null)
                        }
                    }
                    _event.send(ManageProductUiEvent.ShowSuccess(Resources.String.ProductSavedSuccessfully))
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
                is AppResult.Success<*> -> {
                    _event.send(ManageProductUiEvent.ShowSuccess(Resources.String.ProductDeletedSuccessfully))
                    _event.send(ManageProductUiEvent.NavigateBack)
                }

                is AppResult.Error -> {
                    _event.send(ManageProductUiEvent.ShowError(result.message))
                }

                is AppResult.Loading -> {}
            }
        }
    }

    private fun deleteImage(imageId: Long) {
        viewModelScope.launch {
            if (productId == -1L) {
                _state.update { it.copy(images = it.images.filter { img -> img.id != imageId }) }
            } else {
                when (val result = deleteProductImageUseCase(productId, imageId)) {
                    is AppResult.Success -> {
                        _event.send(ManageProductUiEvent.ShowSuccess(Resources.String.ImageDeleted))
                        loadProductDetail()
                    }

                    is AppResult.Error -> _event.send(ManageProductUiEvent.ShowError(result.message))
                    is AppResult.Loading -> {}
                }
            }
        }
    }

    private fun addVariant(
        optionType: String,
        optionValue: String,
        sku: String,
        price: Double,
        initialOnHand: Int
    ) {
        if (productId == -1L) {
            _state.update {
                val newVariant = AdminVariant(
                    id = -(it.variants.size + 1).toLong(),
                    sku = sku,
                    price = price,
                    compareAtPrice = null,
                    isActive = true,
                    onHand = initialOnHand,
                    reserved = 0,
                    options = mapOf(optionType to optionValue)
                )
                it.copy(variants = it.variants + newVariant)
            }
            return
        }
        viewModelScope.launch {
            val result = createProductVariantUseCase(
                productId = productId,
                optionType = optionType,
                optionValue = optionValue,
                sku = sku,
                price = price,
                compareAtPrice = null,
                isActive = true,
                initialOnHand = initialOnHand
            )
            when (result) {
                is AppResult.Success<*> -> {
                    _event.send(ManageProductUiEvent.ShowSuccess(Resources.String.VariantAdded))
                    loadProductDetail()
                }

                is AppResult.Error -> _event.send(ManageProductUiEvent.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }

    private fun updateVariant(
        id: Long,
        sku: String?,
        price: Double?,
        optionType: String?,
        optionValue: String?,
        isActive: Boolean?
    ) {
        if (productId == -1L) {
            _state.update { state ->
                val updatedVariants = state.variants.map { v ->
                    if (v.id == id) {
                        v.copy(
                            sku = sku ?: v.sku,
                            price = price ?: v.price,
                            options = if (optionType != null && optionValue != null) mapOf(optionType to optionValue) else v.options,
                            isActive = isActive ?: v.isActive
                        )
                    } else v
                }
                state.copy(variants = updatedVariants)
            }
            return
        }
        viewModelScope.launch {
            when (val result =
                updateProductVariantUseCase(id, sku, price, null, optionType, optionValue, isActive)) {
                is AppResult.Success<*> -> {
                    _event.send(ManageProductUiEvent.ShowSuccess(Resources.String.VariantUpdated))
                    loadProductDetail()
                }

                is AppResult.Error -> _event.send(ManageProductUiEvent.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }

    private fun deleteVariant(variantId: Long) {
        if (productId == -1L) {
            _state.update { it.copy(variants = it.variants.filter { v -> v.id != variantId }) }
            return
        }
        viewModelScope.launch {
            when (val result = deleteProductVariantUseCase(variantId)) {
                is AppResult.Success<*> -> {
                    _event.send(ManageProductUiEvent.ShowSuccess(Resources.String.VariantDeleted))
                    loadProductDetail()
                }

                is AppResult.Error -> _event.send(ManageProductUiEvent.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }

    private fun createCategory(name: String, slug: String, parentId: Long?) {
        viewModelScope.launch {
            when (val result = createAdminCategoryUseCase(name, slug, parentId)) {
                is AppResult.Success<*> -> {
                    _event.send(ManageProductUiEvent.ShowSuccess(Resources.String.CategoryCreated))
                    loadInitialData()
                }

                is AppResult.Error -> _event.send(ManageProductUiEvent.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }

    private fun deleteCategory(id: Long) {
        viewModelScope.launch {
            when (val result = deleteAdminCategoryUseCase(id)) {
                is AppResult.Success -> {
                    _event.send(ManageProductUiEvent.ShowSuccess(Resources.String.CategoryDeleted))
                    loadInitialData()
                }

                is AppResult.Error -> _event.send(ManageProductUiEvent.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }

    private fun uploadImage(bytes: ByteArray) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            if (productId != -1L) {
                when (val result = addProductImageUseCase(productId, bytes, null)) {
                    is AppResult.Success<*> -> {
                        _event.send(ManageProductUiEvent.ShowSuccess(Resources.String.ImageUploaded))
                        loadProductDetail()
                    }

                    is AppResult.Error -> _event.send(ManageProductUiEvent.ShowError(result.message))
                    is AppResult.Loading -> {}
                }
            } else {
                _state.update { it.copy(selectedImageBytes = it.selectedImageBytes + bytes) }
                _event.send(ManageProductUiEvent.ShowSuccess(Resources.String.ImageSelected))
            }
            _state.update { it.copy(isSaving = false) }
        }
    }
}

data class ManageProductState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val title: String = "",
    val slug: String = "",
    val description: String = "",
    val basePrice: Double = 0.0,
    val isActive: Boolean = true,
    val selectedCategory: Category? = null,
    val categories: List<Category> = emptyList(),
    val availableOptions: List<AdminOption> = emptyList(),
    val images: List<ProductImageUiModel> = emptyList(),
    val selectedImageBytes: List<ByteArray> = emptyList(),
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
    data class SelectCategory(val category: Category) : ManageProductIntent
    data object SaveProduct : ManageProductIntent
    data object DeleteProduct : ManageProductIntent
    data class DeleteImage(val imageId: Long) : ManageProductIntent
    data class AddVariant(
        val optionType: String,
        val optionValue: String,
        val sku: String,
        val price: Double,
        val initialOnHand: Int
    ) : ManageProductIntent

    data class UpdateVariantInfo(
        val id: Long,
        val sku: String?,
        val price: Double?,
        val optionType: String?,
        val optionValue: String?,
        val isActive: Boolean?
    ) : ManageProductIntent

    data class DeleteVariant(val variantId: Long) : ManageProductIntent

    data class CreateCategory(
        val name: String,
        val slug: String,
        val parentId: Long?
    ) : ManageProductIntent

    data class DeleteCategory(val id: Long) : ManageProductIntent

    data class UploadImage(val bytes: ByteArray) : ManageProductIntent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as UploadImage
            return bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int {
            return bytes.contentHashCode()
        }
    }
}

sealed class ManageProductUiEvent {
    data class ShowError(val message: Any) : ManageProductUiEvent()
    data class ShowSuccess(val message: Any) : ManageProductUiEvent()
    data object NavigateBack : ManageProductUiEvent()
}
