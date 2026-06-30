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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.Resources
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ManageProductScreen(
    id: Long?,
    navigateBack: () -> Unit,
) {
    val messageBarState = rememberMessageBarState()
    val viewModel = koinViewModel<ManageProductViewModel>()
    val state by viewModel.state.collectAsState()
    val mediaPicker = remember { MediaPicker() }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    mediaPicker.InitializeMediaPicker(
        onMediaSelect = { bytes, isVideo ->
            if (isVideo) {
                viewModel.handleIntent(ManageProductIntent.UploadVideo(bytes))
            } else {
                viewModel.handleIntent(ManageProductIntent.UploadImage(bytes))
            }
        }
    )

    var showCategoriesBottomSheet by remember { mutableStateOf(false) }
    var showAddVariantDialog by remember { mutableStateOf(false) }
    var showBulkVariantDialog by remember { mutableStateOf(false) }
    var selectedVariantToEdit by remember { mutableStateOf<AdminVariant?>(null) }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var showDeactivationDialog by remember { mutableStateOf(false) }
    var dropdownMenuOpened by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ManageProductEffect.ShowError -> messageBarState.addError(effect.message)
                is ManageProductEffect.ShowSuccess -> messageBarState.addSuccess(effect.message)
                is ManageProductEffect.ShowDeactivationSuggestion -> showDeactivationDialog = true
                is ManageProductEffect.NavigateBack -> navigateBack()
            }
        }
    }

    if (showDeactivationDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivationDialog = false },
            title = { Text(stringResource(Resources.String.WarningText)) },
            text = { Text(stringResource(Resources.String.ProductDeactivationSuggestion)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.handleIntent(ManageProductIntent.DeactivateProduct)
                        showDeactivationDialog = false
                    }
                ) {
                    Text(stringResource(Resources.String.Deactivate))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivationDialog = false }) {
                    Text(stringResource(Resources.String.Cancel))
                }
            }
        )
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
            onConfirm = { sku, price, discountedPrice, options, isActive, initialOnHand, shouldDismiss ->
                viewModel.handleIntent(
                    ManageProductIntent.AddVariant(
                        options = options,
                        sku = sku,
                        price = price,
                        discountedPrice = discountedPrice,
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
            onConfirm = { sku, price, discountedPrice, options, isActive, initialOnHand, shouldDismiss ->
                viewModel.handleIntent(
                    ManageProductIntent.UpdateVariantInfo(
                        id = variant.id,
                        sku = sku,
                        price = price,
                        discountedPrice = discountedPrice,
                        options = options,
                        isActive = isActive,
                        onHand = initialOnHand
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

                    // ---- دسته‌بندی (چیپ‌های انتخابی، مطابق مرجع) ----
                    FieldLabel(stringResource(Resources.String.SelectCategory))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.categories.forEach { category ->
                            CategoryChip(
                                label = category.name,
                                selected = state.selectedCategory?.id == category.id,
                                onClick = { viewModel.handleIntent(ManageProductIntent.SelectCategory(category)) }
                            )
                        }
                        CategoryChip(
                            label = "+ دسته جدید",
                            selected = false,
                            onClick = { showCreateCategoryDialog = true }
                        )
                    }

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
                        CustomTextField(
                            modifier = Modifier.weight(1f),
                            value = if (state.discountedPrice == 0.0) "" else state.discountedPrice.toString(),
                            onValueChange = {
                                val price = it.toDoubleOrNull() ?: 0.0
                                viewModel.handleIntent(ManageProductIntent.UpdateDiscountedPrice(price))
                            },
                            placeholder = stringResource(Resources.String.DiscountedPricePlaceholder),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    if (state.variants.isEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CustomTextField(
                                modifier = Modifier.weight(1f),
                                value = state.sku,
                                onValueChange = { viewModel.handleIntent(ManageProductIntent.UpdateSku(it)) },
                                placeholder = stringResource(Resources.String.Sku)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            CustomTextField(
                                modifier = Modifier.weight(1f),
                                value = if (state.initialOnHand == 0) "" else state.initialOnHand.toString(),
                                onValueChange = {
                                    val qty = it.toIntOrNull() ?: 0
                                    viewModel.handleIntent(ManageProductIntent.UpdateInitialOnHand(qty))
                                },
                                placeholder = stringResource(Resources.String.InitialStock),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
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

                    // Images & Videos Section
                    Text("تصاویر و ویدئوها", fontWeight = FontWeight.Bold, fontSize = FontSize.MEDIUM)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.images) { image ->
                            Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(Radius.xs))) {
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
                                        tint = AppTheme.colors.sale
                                    )
                                }
                            }
                        }
                        items(state.videos) { video ->
                            Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(Radius.xs))) {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painterResource(Resources.Icon.RightArrow),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.handleIntent(
                                            ManageProductIntent.DeleteVideo(
                                                video.id
                                            )
                                        )
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                                ) {
                                    Icon(
                                        painterResource(Resources.Icon.Close),
                                        contentDescription = null,
                                        tint = AppTheme.colors.sale
                                    )
                                }
                            }
                        }
                        items(state.selectedImageBytes) { bytes ->
                            Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(Radius.xs))) {
                                AsyncImage(
                                    model = bytes,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        items(state.selectedVideoBytes) { bytes ->
                            Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(Radius.xs))) {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painterResource(Resources.Icon.RightArrow),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(Radius.xs))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(Radius.xs)
                                    )
                                    .clickable { mediaPicker.open() },
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.isSaving) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            painterResource(Resources.Icon.Plus),
                                            contentDescription = null
                                        )
                                        Text("رسانه", fontSize = FontSize.EXTRA_SMALL)
                                    }
                                }
                            }
                        }
                    }

                    // ---- تنوع محصول (واریانت‌ها) — مطابق مرجع ----
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تنوع محصول (واریانت‌ها) ${state.variants.size} مورد",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = FontSize.EXTRA_REGULAR,
                                color = AppTheme.colors.onSurface
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = "برای هر ترکیب رنگ/سایز/جنس، قیمت و موجودی جداگانه تعریف کنید.",
                                fontSize = FontSize.SMALL,
                                color = AppTheme.colors.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "+ افزودن واریانت",
                            modifier = Modifier.clickable { showAddVariantDialog = true },
                            color = AppTheme.colors.primary,
                            fontSize = FontSize.SMALL,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (state.variants.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.md))
                                .border(1.dp, AppTheme.colors.line, RoundedCornerShape(Radius.md))
                                .clickable { showAddVariantDialog = true }
                                .padding(vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("هنوز واریانتی تعریف نشده", fontWeight = FontWeight.Bold, fontSize = FontSize.REGULAR, color = AppTheme.colors.onSurface)
                            Spacer(Modifier.height(5.dp))
                            Text("برای افزودن رنگ، سایز و موجودی روی این کادر بزنید", fontSize = FontSize.SMALL, color = AppTheme.colors.onSurfaceVariant)
                        }
                    } else {
                        val masterKeys = state.variants.firstOrNull()?.options?.keys ?: emptySet()
                        state.variants.forEach { variant ->
                            val isInvalid = !variant.options.keys.containsAll(masterKeys) && masterKeys.isNotEmpty()
                            VariantDisplayCard(
                                variant = variant,
                                isInvalid = isInvalid,
                                onEdit = { selectedVariantToEdit = variant },
                                onDelete = { viewModel.handleIntent(ManageProductIntent.DeleteVariant(variant.id)) }
                            )
                        }
                        Text(
                            text = "+ تولید گروهیِ واریانت‌ها",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showBulkVariantDialog = true }
                                .padding(vertical = 6.dp),
                            color = AppTheme.colors.primary,
                            fontSize = FontSize.SMALL,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    // ---- ذخیره / انصراف ----
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PrimaryButton(
                            modifier = Modifier.weight(1f),
                            text = if (id == null) stringResource(Resources.String.AddProduct) else stringResource(Resources.String.UpdateProduct),
                            enabled = !state.isSaving,
                            onClick = { viewModel.handleIntent(ManageProductIntent.SaveProduct) }
                        )
                        OutlinedButton(
                            onClick = navigateBack,
                            modifier = Modifier.weight(0.5f).height(52.dp)
                        ) {
                            Text(stringResource(Resources.String.Cancel))
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

/** برچسبِ بالای فیلد (مطابق مرجع). */
@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = FontSize.SMALL,
        color = AppTheme.colors.onSurface
    )
}

/** چیپِ انتخابِ دسته‌بندی. */
@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .then(
                if (selected) Modifier.background(colors.primary)
                else Modifier.background(colors.surface).border(1.dp, colors.line, RoundedCornerShape(11.dp))
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 9.dp),
        fontSize = FontSize.SMALL,
        fontWeight = FontWeight.Bold,
        color = if (selected) colors.onPrimary else colors.onSurfaceVariant,
        maxLines = 1
    )
}

/** کارتِ نمایشِ واریانت — مطابق مرجع: عنوان + نشانِ فعال + ویرایش/حذف + خط SKU/قیمت/موجودی. */
@Composable
private fun VariantDisplayCard(
    variant: AdminVariant,
    isInvalid: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = AppTheme.colors
    val priceText = stringResource(Resources.String.PriceFormat, variant.discountedPrice ?: variant.price)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .border(
                width = if (isInvalid) 1.5.dp else 1.dp,
                color = if (isInvalid) colors.sale else colors.line,
                shape = RoundedCornerShape(Radius.md)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = variant.options.entries.joinToString(" · ") { "${it.key}: ${it.value}" }.ifBlank { variant.sku },
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                fontSize = FontSize.SMALL,
                color = colors.onSurface
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (variant.isActive) "فعال" else "غیرفعال",
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (variant.isActive) colors.ok.copy(alpha = 0.12f) else colors.surfaceVariant)
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                fontSize = FontSize.EXTRA_SMALL,
                fontWeight = FontWeight.Bold,
                color = if (variant.isActive) colors.ok else colors.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(colors.accentSoft).clickable { onEdit() },
                contentAlignment = Alignment.Center
            ) { Icon(painterResource(Resources.Icon.Edit), contentDescription = null, tint = colors.primary, modifier = Modifier.size(15.dp)) }
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(colors.sale.copy(alpha = 0.1f)).clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) { Icon(painterResource(Resources.Icon.Delete), contentDescription = null, tint = colors.sale, modifier = Modifier.size(15.dp)) }
        }
        Spacer(Modifier.height(9.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("SKU: ${variant.sku}", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
            Text("قیمت: $priceText", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
            Text("موجودی: ${variant.onHand}", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
        }
    }
}
