package com.kazemieh.admin_panel.manage_product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Category
import com.kazemieh.domain.model.admin.AdminVariant
import com.kazemieh.domain.repository.Color
import com.kazemieh.domain.repository.Size
import com.kazemieh.domain.usecase.admin.*
import com.kazemieh.domain.usecase.catalog.GetCategoriesUseCase
import com.kazemieh.common.ld
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
    private val createSizeUseCase: CreateSizeUseCase,
    private val updateSizeUseCase: UpdateSizeUseCase,
    private val deleteSizeUseCase: DeleteSizeUseCase,
    private val createColorUseCase: CreateColorUseCase,
    private val updateColorUseCase: UpdateColorUseCase,
    private val deleteColorUseCase: DeleteColorUseCase,
    private val addProductImageUseCase: AddProductImageUseCase,
    private val deleteProductImageUseCase: DeleteProductImageUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getAdminSizesUseCase: GetAdminSizesUseCase,
    private val getAdminColorsUseCase: GetAdminColorsUseCase,
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
        "Received intent: ${intent::class.simpleName}".ld("ManageProductVM")
        when (intent) {
            is ManageProductIntent.UpdateTitle -> _state.update { it.copy(title = intent.title) }
            is ManageProductIntent.UpdateDescription -> _state.update { it.copy(description = intent.description) }
            is ManageProductIntent.UpdateBasePrice -> _state.update { it.copy(basePrice = intent.price) }
            is ManageProductIntent.UpdateIsActive -> _state.update { it.copy(isActive = intent.isActive) }
            is ManageProductIntent.SelectCategory -> _state.update { it.copy(selectedCategory = intent.category) }
            is ManageProductIntent.SelectSize -> _state.update { it.copy(selectedSize = intent.size) }
            is ManageProductIntent.SelectColor -> _state.update { it.copy(selectedColor = intent.color) }
            is ManageProductIntent.SaveProduct -> saveProduct()
            is ManageProductIntent.DeleteProduct -> deleteProduct()
            is ManageProductIntent.DeleteImage -> deleteImage(intent.imageId)
            is ManageProductIntent.AddVariant -> addVariant(intent)
            is ManageProductIntent.UpdateVariantInfo -> updateVariant(intent.id, intent.sku, intent.price, intent.isActive)
            is ManageProductIntent.DeleteVariant -> deleteVariant(intent.variantId)
            is ManageProductIntent.CreateCategory -> createCategory(intent.name, intent.slug, intent.parentId)
            is ManageProductIntent.DeleteCategory -> deleteCategory(intent.id)
            is ManageProductIntent.CreateSize -> createSize(intent.name, intent.sortOrder)
            is ManageProductIntent.UpdateSizeInfo -> updateSize(intent.id, intent.name, intent.sortOrder)
            is ManageProductIntent.DeleteSize -> deleteSize(intent.id)
            is ManageProductIntent.CreateColor -> createColor(intent.name, intent.hex)
            is ManageProductIntent.UpdateColorInfo -> updateColor(intent.id, intent.name, intent.hex)
            is ManageProductIntent.DeleteColor -> deleteColor(intent.id)
            is ManageProductIntent.UploadImage -> uploadImage(intent.bytes)
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val categoriesResult = getCategoriesUseCase()
            val sizesResult = getAdminSizesUseCase()
            val colorsResult = getAdminColorsUseCase()
            _state.update {
                it.copy(
                    categories = if (categoriesResult is AppResult.Success) categoriesResult.data else emptyList(),
                    sizes = if (sizesResult is AppResult.Success) sizesResult.data else emptyList(),
                    colors = if (colorsResult is AppResult.Success) colorsResult.data else emptyList()
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
                is AppResult.Success<*> -> {
                    val newId = (result.data as? com.kazemieh.domain.model.admin.AdminProduct)?.id
                    if (productId == -1L && newId != null) {
                        currentState.selectedImageBytes.forEach { bytes ->
                            addProductImageUseCase(newId, bytes, null)
                        }
                    }
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
                is AppResult.Success<*> -> {
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

    private fun deleteImage(imageId: Long) {
        viewModelScope.launch {
            if (productId == -1L) {
                _state.update { it.copy(images = it.images.filter { img -> img.id != imageId }) }
            } else {
                when (val result = deleteProductImageUseCase(productId, imageId)) {
                    is AppResult.Success -> {
                        _event.send(ManageProductUiEvent.ShowSuccess("Image deleted"))
                        loadProductDetail()
                    }
                    is AppResult.Error -> _event.send(ManageProductUiEvent.ShowError(result.message))
                    is AppResult.Loading -> {}
                }
            }
        }
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
                is AppResult.Success<*> -> {
                    _event.send(ManageProductUiEvent.ShowSuccess("Variant added"))
                    loadProductDetail()
                }

                is AppResult.Error -> _event.send(ManageProductUiEvent.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }

    private fun updateVariant(id: Long, sku: String?, price: Double?, isActive: Boolean?) {
        viewModelScope.launch {
            when (val result = updateProductVariantUseCase(id, sku, price, null, isActive)) {
                is AppResult.Success<*> -> {
                    _event.send(ManageProductUiEvent.ShowSuccess("Variant updated"))
                    loadProductDetail()
                }
                is AppResult.Error -> _event.send(ManageProductUiEvent.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }

    private fun deleteVariant(variantId: Long) {
        viewModelScope.launch {
            when (val result = deleteProductVariantUseCase(variantId)) {
                is AppResult.Success<*> -> {
                    _event.send(ManageProductUiEvent.ShowSuccess("Variant deleted"))
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
                    _event.send(ManageProductUiEvent.ShowSuccess("Category created"))
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
                    _event.send(ManageProductUiEvent.ShowSuccess("Category deleted"))
                    loadInitialData()
                }
                is AppResult.Error -> _event.send(ManageProductUiEvent.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }

    private fun createSize(name: String, sortOrder: Int) {
        viewModelScope.launch {
            when (val result = createSizeUseCase(name, sortOrder)) {
                is AppResult.Success<*> -> {
                    _event.send(ManageProductUiEvent.ShowSuccess("Size created"))
                    loadInitialData()
                }
                is AppResult.Error -> _event.send(ManageProductUiEvent.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }

    private fun updateSize(id: Long, name: String?, sortOrder: Int?) {
        viewModelScope.launch {
            when (val result = updateSizeUseCase(id, name, sortOrder)) {
                is AppResult.Success<*> -> {
                    _event.send(ManageProductUiEvent.ShowSuccess("Size updated"))
                    loadInitialData()
                }
                is AppResult.Error -> _event.send(ManageProductUiEvent.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }

    private fun deleteSize(id: Long) {
        "Deleting size: $id".ld("ManageProductVM")
        viewModelScope.launch {
            when (val result = deleteSizeUseCase(id)) {
                is AppResult.Success<*> -> {
                    "Size deleted successfully".ld("ManageProductVM")
                    _event.send(ManageProductUiEvent.ShowSuccess("Size deleted"))
                    loadInitialData()
                }
                is AppResult.Error -> {
                    "Error deleting size: ${result.message}".ld("ManageProductVM")
                    _event.send(ManageProductUiEvent.ShowError(result.message))
                }
                is AppResult.Loading -> {}
            }
        }
    }

    private fun createColor(name: String, hex: String?) {
        viewModelScope.launch {
            when (val result = createColorUseCase(name, hex)) {
                is AppResult.Success<*> -> {
                    _event.send(ManageProductUiEvent.ShowSuccess("Color created"))
                    loadInitialData()
                }
                is AppResult.Error -> _event.send(ManageProductUiEvent.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }

    private fun updateColor(id: Long, name: String?, hex: String?) {
        viewModelScope.launch {
            when (val result = updateColorUseCase(id, name, hex)) {
                is AppResult.Success<*> -> {
                    _event.send(ManageProductUiEvent.ShowSuccess("Color updated"))
                    loadInitialData()
                }
                is AppResult.Error -> _event.send(ManageProductUiEvent.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }

    private fun deleteColor(id: Long) {
        "Deleting color: $id".ld("ManageProductVM")
        viewModelScope.launch {
            when (val result = deleteColorUseCase(id)) {
                is AppResult.Success<*> -> {
                    "Color deleted successfully".ld("ManageProductVM")
                    _event.send(ManageProductUiEvent.ShowSuccess("Color deleted"))
                    loadInitialData()
                }
                is AppResult.Error -> {
                    "Error deleting color: ${result.message}".ld("ManageProductVM")
                    _event.send(ManageProductUiEvent.ShowError(result.message))
                }
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
                        _event.send(ManageProductUiEvent.ShowSuccess("Image uploaded"))
                        loadProductDetail()
                    }
                    is AppResult.Error -> _event.send(ManageProductUiEvent.ShowError(result.message))
                    is AppResult.Loading -> {}
                }
            } else {
                _state.update { it.copy(selectedImageBytes = it.selectedImageBytes + bytes) }
                _event.send(ManageProductUiEvent.ShowSuccess("Image selected"))
            }
            _state.update { it.copy(isSaving = false) }
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
    val selectedSize: Size? = null,
    val selectedColor: Color? = null,
    val categories: List<Category> = emptyList(),
    val sizes: List<Size> = emptyList(),
    val colors: List<Color> = emptyList(),
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
    data class SelectSize(val size: Size) : ManageProductIntent
    data class SelectColor(val color: Color) : ManageProductIntent
    data object SaveProduct : ManageProductIntent
    data object DeleteProduct : ManageProductIntent
    data class DeleteImage(val imageId: Long) : ManageProductIntent
    data class AddVariant(
        val sizeId: Long,
        val colorId: Long,
        val sku: String,
        val price: Double,
        val initialOnHand: Int
    ) : ManageProductIntent
    data class UpdateVariantInfo(
        val id: Long,
        val sku: String?,
        val price: Double?,
        val isActive: Boolean?
    ) : ManageProductIntent
    data class DeleteVariant(val variantId: Long) : ManageProductIntent

    data class CreateCategory(
        val name: String,
        val slug: String,
        val parentId: Long?
    ) : ManageProductIntent
    data class DeleteCategory(val id: Long) : ManageProductIntent

    data class CreateSize(
        val name: String,
        val sortOrder: Int
    ) : ManageProductIntent

    data class UpdateSizeInfo(
        val id: Long,
        val name: String?,
        val sortOrder: Int?
    ) : ManageProductIntent

    data class DeleteSize(val id: Long) : ManageProductIntent

    data class CreateColor(
        val name: String,
        val hex: String?
    ) : ManageProductIntent

    data class UpdateColorInfo(
        val id: Long,
        val name: String?,
        val hex: String?
    ) : ManageProductIntent

    data class DeleteColor(val id: Long) : ManageProductIntent

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
    data class ShowError(val message: String) : ManageProductUiEvent()
    data class ShowSuccess(val message: String) : ManageProductUiEvent()
    data object NavigateBack : ManageProductUiEvent()
}
