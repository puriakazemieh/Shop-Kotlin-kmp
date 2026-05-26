package com.kazemieh.admin.products

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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
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
import com.kazemieh.domain.admin.AdminVariant
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
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    photoPicker.InitializePhotoPicker(
        onImageSelect = { bytes ->
            viewModel.handleIntent(ManageProductIntent.UploadImage(bytes))
        }
    )

    var showCategoriesBottomSheet by remember { mutableStateOf(false) }
    var showAddVariantDialog by remember { mutableStateOf(false) }
    var showBulkVariantDialog by remember { mutableStateOf(false) }
    var selectedVariantToEdit by remember { mutableStateOf<AdminVariant?>(null) }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var dropdownMenuOpened by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ManageProductEffect.ShowError -> messageBarState.addError(effect.message)
                is ManageProductEffect.ShowSuccess -> messageBarState.addSuccess(effect.message)
                is ManageProductEffect.NavigateBack -> navigateBack()
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
        VariantBottomSheet(
            availableOptions = state.availableOptions,
            existingVariants = state.variants,
            defaultOptionTypes = state.defaultOptionTypes,
            onDismiss = { showAddVariantDialog = false },
            onConfirm = { sku, price, options, isActive, initialOnHand, shouldDismiss ->
                viewModel.handleIntent(
                    ManageProductIntent.AddVariant(
                        options = options,
                        sku = sku,
                        price = price,
                        initialOnHand = initialOnHand
                    )
                )
                if (shouldDismiss) showAddVariantDialog = false
            },
            onApplyToAll = { viewModel.handleIntent(ManageProductIntent.ApplyPropertyToAll(it)) },
            onCreateOptionType = { viewModel.handleIntent(ManageProductIntent.CreateOptionType(it)) },
            onCreateOptionValue = { id, value ->
                viewModel.handleIntent(
                    ManageProductIntent.CreateOptionValue(
                        id,
                        value
                    )
                )
            },
            onCreateOptionTypeAndValue = { type, value ->
                viewModel.handleIntent(
                    ManageProductIntent.CreateOptionTypeAndValue(
                        type,
                        value
                    )
                )
            }
        )
    }

    if (showBulkVariantDialog) {
        BulkVariantBottomSheet(
            availableOptions = state.availableOptions,
            onDismiss = { showBulkVariantDialog = false },
            onGenerate = { combinations ->
                viewModel.handleIntent(ManageProductIntent.BulkAddVariants(combinations))
                showBulkVariantDialog = false
            },
            onCreateOptionType = { viewModel.handleIntent(ManageProductIntent.CreateOptionType(it)) },
            onCreateOptionValue = { id, value -> viewModel.handleIntent(ManageProductIntent.CreateOptionValue(id, value)) }
        )
    }

    selectedVariantToEdit?.let { variant ->
        VariantBottomSheet(
            variant = variant,
            availableOptions = state.availableOptions,
            existingVariants = state.variants,
            defaultOptionTypes = state.defaultOptionTypes,
            onDismiss = { selectedVariantToEdit = null },
            onConfirm = { sku, price, options, isActive, initialOnHand, shouldDismiss ->
                viewModel.handleIntent(
                    ManageProductIntent.UpdateVariantInfo(
                        id = variant.id,
                        sku = sku,
                        price = price,
                        options = options,
                        isActive = isActive
                    )
                )
                if (shouldDismiss) selectedVariantToEdit = null
            },
            onApplyToAll = { viewModel.handleIntent(ManageProductIntent.ApplyPropertyToAll(it)) },
            onDelete = {
                viewModel.handleIntent(ManageProductIntent.DeleteVariant(variant.id))
                selectedVariantToEdit = null
            },
            onCreateOptionType = { viewModel.handleIntent(ManageProductIntent.CreateOptionType(it)) },
            onCreateOptionValue = { id, value ->
                viewModel.handleIntent(
                    ManageProductIntent.CreateOptionValue(
                        id,
                        value
                    )
                )
            },
            onCreateOptionTypeAndValue = { type, value ->
                viewModel.handleIntent(
                    ManageProductIntent.CreateOptionTypeAndValue(
                        type,
                        value
                    )
                )
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (id == null) stringResource(Resources.String.NewProduct) else stringResource(Resources.String.EditProduct),
                        fontSize = FontSize.LARGE,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            modifier = Modifier.graphicsLayer { rotationY = if (isRtl) 180f else 0f },
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = stringResource(Resources.String.BackArrowIconDesc),
                            tint = MaterialTheme.colorScheme.onSurface
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
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                containerColor = MaterialTheme.colorScheme.surface,
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
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
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
                    
                    val masterKeys = state.variants.firstOrNull()?.options?.keys ?: emptySet()
                    
                    state.variants.forEach { variant ->
                        val currentKeys = variant.options.keys
                        // A variant is only invalid if it LACKS a property that the master variant has.
                        val isInvalid = !currentKeys.containsAll(masterKeys) && masterKeys.isNotEmpty()

                        VariantItem(
                            variant = variant,
                            isInvalid = isInvalid,
                            onClick = { selectedVariantToEdit = variant },
                            onFieldUpdate = { sku, price, onHand ->
                                viewModel.handleIntent(ManageProductIntent.UpdateVariantInline(variant.id, sku, price, onHand))
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showBulkVariantDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(painterResource(Resources.Icon.Plus), contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(Resources.String.BulkAddVariants))
                        }
                        
                        Button(
                            onClick = { showAddVariantDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(painterResource(Resources.Icon.Plus), contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(Resources.String.AddVariant))
                        }
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
fun VariantItem(
    variant: AdminVariant, 
    isInvalid: Boolean = false, 
    onClick: () -> Unit,
    onFieldUpdate: (sku: String?, price: Double?, onHand: Int?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().then(
            if (isInvalid) Modifier.border(2.dp, MaterialTheme.colorScheme.error, CardDefaults.shape)
            else Modifier
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = variant.options.entries.joinToString(", ") { "${it.key}: ${it.value}" },
                    fontSize = FontSize.MEDIUM,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).clickable { onClick() }
                )
                IconButton(onClick = onClick, modifier = Modifier.size(24.dp)) {
                    Icon(painterResource(Resources.Icon.Edit), contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = variant.sku,
                    onValueChange = { onFieldUpdate(it, null, null) },
                    label = { Text(stringResource(Resources.String.Sku), fontSize = FontSize.EXTRA_SMALL) },
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(fontSize = FontSize.SMALL),
                    singleLine = true
                )
                OutlinedTextField(
                    value = if (variant.price == 0.0) "" else variant.price.toString(),
                    onValueChange = { onFieldUpdate(null, it.toDoubleOrNull() ?: 0.0, null) },
                    label = { Text(stringResource(Resources.String.Price), fontSize = FontSize.EXTRA_SMALL) },
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(fontSize = FontSize.SMALL),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = variant.onHand.toString(),
                    onValueChange = { onFieldUpdate(null, null, it.toIntOrNull() ?: 0) },
                    label = { Text(stringResource(Resources.String.InitialStock), fontSize = FontSize.EXTRA_SMALL) },
                    modifier = Modifier.weight(0.8f),
                    textStyle = LocalTextStyle.current.copy(fontSize = FontSize.SMALL),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }
    }
}
