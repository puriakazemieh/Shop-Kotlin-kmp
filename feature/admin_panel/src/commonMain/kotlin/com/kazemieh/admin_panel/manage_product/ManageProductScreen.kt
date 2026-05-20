package com.kazemieh.admin_panel.manage_product

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.AlertTextField
import com.kazemieh.designsystem.component.CustomTextField
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.component.PrimaryButton
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.model.admin.AdminVariant
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProductScreen(
    id: Long?,
    navigateBack: () -> Unit,
) {
    val messageBarState = rememberMessageBarState()
    val viewModel = koinViewModel<ManageProductViewModel>()
    val state by viewModel.state.collectAsState()
    val photoPicker = remember { PhotoPicker() }

    photoPicker.InitializePhotoPicker(
        onImageSelect = { bytes ->
            viewModel.handleIntent(ManageProductIntent.UploadImage(bytes))
        }
    )

    var showCategoriesBottomSheet by remember { mutableStateOf(false) }
    var showAddVariantDialog by remember { mutableStateOf(false) }
    var selectedVariantToEdit by remember { mutableStateOf<AdminVariant?>(null) }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var dropdownMenuOpened by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is ManageProductUiEvent.ShowError -> messageBarState.addError(event.message)
                is ManageProductUiEvent.ShowSuccess -> messageBarState.addSuccess(event.message)
                is ManageProductUiEvent.NavigateBack -> navigateBack()
            }
        }
    }

    if (showCategoriesBottomSheet) {
        CategoriesBottomSheet(
            categories = state.categories,
            onCategorySelected = { viewModel.handleIntent(ManageProductIntent.SelectCategory(it)) },
            onCreateCategoryClick = {
                showCreateCategoryDialog = true
                showCategoriesBottomSheet = false
            },
            onDeleteCategory = { viewModel.handleIntent(ManageProductIntent.DeleteCategory(it)) },
            onDismiss = { showCategoriesBottomSheet = false }
        )
    }

    if (showCreateCategoryDialog) {
        CreateCategoryDialog(
            onDismiss = { showCreateCategoryDialog = false },
            onConfirm = { name, slug ->
                viewModel.handleIntent(ManageProductIntent.CreateCategory(name, slug, null))
                showCreateCategoryDialog = false
            }
        )
    }

    if (showAddVariantDialog) {
        AddVariantDialog(
            sizes = state.sizes,
            colors = state.colors,
            onDismiss = { showAddVariantDialog = false },
            onConfirm = { sizeId, colorId, sku, price, initialOnHand ->
                viewModel.handleIntent(
                    ManageProductIntent.AddVariant(
                        sizeId = sizeId,
                        colorId = colorId,
                        sku = sku,
                        price = price,
                        initialOnHand = initialOnHand
                    )
                )
                showAddVariantDialog = false
            },
            onCreateSize = { name, sortOrder ->
                viewModel.handleIntent(ManageProductIntent.CreateSize(name, sortOrder))
            },
            onDeleteSize = { viewModel.handleIntent(ManageProductIntent.DeleteSize(it)) },
            onCreateColor = { name, hex ->
                viewModel.handleIntent(ManageProductIntent.CreateColor(name, hex))
            },
            onDeleteColor = { viewModel.handleIntent(ManageProductIntent.DeleteColor(it)) }
        )
    }

    selectedVariantToEdit?.let { variant ->
        EditVariantDialog(
            variant = variant,
            sizes = state.sizes,
            colors = state.colors,
            onDismiss = { selectedVariantToEdit = null },
            onConfirm = { sku, price, sizeId, colorId, isActive ->
                viewModel.handleIntent(ManageProductIntent.UpdateVariantInfo(variant.id, sku, price, sizeId, colorId, isActive))
                selectedVariantToEdit = null
            },
            onDelete = {
                viewModel.handleIntent(ManageProductIntent.DeleteVariant(variant.id))
                selectedVariantToEdit = null
            },
            onCreateSize = { name, sortOrder ->
                viewModel.handleIntent(ManageProductIntent.CreateSize(name, sortOrder))
            },
            onCreateColor = { name, hex ->
                viewModel.handleIntent(ManageProductIntent.CreateColor(name, hex))
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (id == null) stringResource(Resources.String.NewProduct) else stringResource(Resources.String.EditProduct),
                        fontSize = FontSize.LARGE,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = stringResource(Resources.String.BackArrowIconDesc),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                actions = {
                    if (id != null) {
                        Box {
                            IconButton(onClick = { dropdownMenuOpened = true }) {
                                Icon(
                                    painter = painterResource(Resources.Icon.VerticalMenu),
                                    contentDescription = stringResource(Resources.String.VerticalMenuIconDesc),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            DropdownMenu(
                                containerColor = MaterialTheme.colorScheme.background,
                                expanded = dropdownMenuOpened,
                                onDismissRequest = { dropdownMenuOpened = false }
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            modifier = Modifier.size(14.dp),
                                            painter = painterResource(Resources.Icon.Delete),
                                            contentDescription = stringResource(Resources.String.DeleteIconDesc),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = stringResource(Resources.String.DeleteProduct),
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = FontSize.REGULAR
                                        )
                                    },
                                    onClick = {
                                        dropdownMenuOpened = false
                                        viewModel.handleIntent(ManageProductIntent.DeleteProduct)
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        ContentWithMessageBar(
            modifier = Modifier.padding(padding),
            messageBarState = messageBarState,
        ) {
            if (state.isLoading) {
                LoadingCard(modifier = Modifier.fillMaxSize())
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState())
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Product Basic Info
                    CustomTextField(
                        value = state.title,
                        onValueChange = { viewModel.handleIntent(ManageProductIntent.UpdateTitle(it)) },
                        placeholder = stringResource(Resources.String.ProductTitlePlaceholder)
                    )

                    CustomTextField(
                        modifier = Modifier.height(120.dp),
                        value = state.description,
                        onValueChange = {
                            viewModel.handleIntent(
                                ManageProductIntent.UpdateDescription(
                                    it
                                )
                            )
                        },
                        placeholder = stringResource(Resources.String.Details),
                        expanded = true
                    )

                    AlertTextField(
                        modifier = Modifier.fillMaxWidth(),
                        text = state.selectedCategory?.name ?: stringResource(Resources.String.SelectCategory),
                        onClick = { showCategoriesBottomSheet = true }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CustomTextField(
                            modifier = Modifier.weight(1f),
                            value = if (state.basePrice == 0.0) "" else state.basePrice.toString(),
                            onValueChange = {
                                val price = it.toDoubleOrNull() ?: 0.0
                                viewModel.handleIntent(ManageProductIntent.UpdateBasePrice(price))
                            },
                            placeholder = stringResource(Resources.String.BasePricePlaceholder),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(Resources.String.Active), fontSize = FontSize.REGULAR)
                            Switch(
                                checked = state.isActive,
                                onCheckedChange = {
                                    viewModel.handleIntent(
                                        ManageProductIntent.UpdateIsActive(
                                            it
                                        )
                                    )
                                }
                            )
                        }
                    }

                    // Images Section
                    Text(stringResource(Resources.String.Details), fontWeight = FontWeight.Bold, fontSize = FontSize.MEDIUM)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.images) { image ->
                            Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp))) {
                                AsyncImage(
                                    model = image.url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = {
                                        viewModel.handleIntent(
                                            ManageProductIntent.DeleteImage(
                                                image.id
                                            )
                                        )
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                                ) {
                                    Icon(
                                        painterResource(Resources.Icon.Close),
                                        contentDescription = null,
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                        items(state.selectedImageBytes) { bytes ->
                            Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp))) {
                                AsyncImage(
                                    model = bytes,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { photoPicker.open() },
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.isSaving) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(
                                        painterResource(Resources.Icon.Plus),
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }

                    // Variants Section
                    Text(stringResource(Resources.String.Variants), fontWeight = FontWeight.Bold, fontSize = FontSize.MEDIUM)
                    state.variants.forEach { variant ->
                        VariantItem(variant) {
                            selectedVariantToEdit = variant
                        }
                    }
                    Button(
                        onClick = { showAddVariantDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(Resources.String.AddVariant))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryButton(
                        text = if (id == null) stringResource(Resources.String.AddProduct) else stringResource(Resources.String.UpdateProduct),
                        enabled = !state.isSaving,
                        onClick = { viewModel.handleIntent(ManageProductIntent.SaveProduct) }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun VariantItem(variant: AdminVariant, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(variant.sku, fontWeight = FontWeight.Bold)
                Text(stringResource(Resources.String.PriceFormat, variant.price), color = MaterialTheme.colorScheme.secondary)
            }
            Text(stringResource(Resources.String.VariantFormat, variant.sizeName, variant.colorName), fontSize = FontSize.SMALL)
            Text(
                "Stock: ${variant.onHand} (Reserved: ${variant.reserved})",
                fontSize = FontSize.SMALL
            )
        }
    }
}
