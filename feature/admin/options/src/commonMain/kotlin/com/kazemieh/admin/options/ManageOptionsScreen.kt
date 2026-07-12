package com.kazemieh.admin.options

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.admin.AdminOption
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageOptionsScreen(
    onBackClick: () -> Unit,
    embedded: Boolean = false
) {
    val viewModel = koinViewModel<ManageOptionsViewModel>()
    val state by viewModel.state.collectAsState()
    val messageBarState = rememberMessageBarState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    var newTypeName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ManageOptionsEffect.ShowError -> messageBarState.addError(effect.message)
                is ManageOptionsEffect.ShowSuccess -> messageBarState.addSuccess(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            if (!embedded) TopAppBar(
                title = {
                    Text(
                        text = stringResource(Resources.String.VariantsManager),
                        fontSize = FontSize.LARGE,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }, // Changed from Settings to Variants
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            modifier = Modifier.graphicsLayer { rotationY = if (isRtl) 180f else 0f },
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = stringResource(Resources.String.BackDesc),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        ContentWithMessageBar(
            modifier = Modifier.padding(padding),
            messageBarState = messageBarState
        ) {
            if (state.isLoading) {
                LoadingCard(modifier = Modifier.fillMaxSize())
            } else {
                val colors = AppTheme.colors
                LazyColumn(
                    modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = "مدیریت ویژگی‌ها و واریانت‌ها",
                            fontSize = FontSize.EXTRA_REGULAR,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "ویژگی‌هایی مثل رنگ، سایز و جنس و مقدارهایشان را اینجا تعریف کنید؛ این مقادیر هنگام ساخت واریانت برای هر محصول در دسترس قرار می‌گیرند.",
                            fontSize = FontSize.SMALL,
                            color = colors.onSurfaceVariant,
                            lineHeight = FontSize.EXTRA_MEDIUM
                        )
                        Spacer(Modifier.height(14.dp))
                        // ردیفِ افزودنِ ویژگیِ جدید (inline)
                        InlineAddRow(
                            value = newTypeName,
                            onValueChange = { newTypeName = it },
                            placeholder = "نام ویژگی جدید (مثلاً طرح)",
                            buttonLabel = "افزودن ویژگی",
                            onSubmit = {
                                if (newTypeName.isNotBlank()) {
                                    viewModel.handleIntent(ManageOptionsIntent.CreateOptionType(newTypeName.trim()))
                                    newTypeName = ""
                                }
                            }
                        )
                    }
                    items(state.options) { option ->
                        OptionTypeItem(
                            option = option,
                            onDeleteType = { viewModel.handleIntent(ManageOptionsIntent.DeleteOptionType(option.id)) },
                            onAddValue = { value -> viewModel.handleIntent(ManageOptionsIntent.CreateOptionValue(option.id, value)) },
                            onDeleteValue = { valueId -> viewModel.handleIntent(ManageOptionsIntent.DeleteOptionValue(valueId)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OptionTypeItem(
    option: AdminOption,
    onDeleteType: () -> Unit,
    onAddValue: (String) -> Unit,
    onDeleteValue: (Long) -> Unit
) {
    val colors = AppTheme.colors
    var valInput by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(colors.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Text(option.name.take(1).uppercase(), fontWeight = FontWeight.ExtraBold, color = colors.primary, fontSize = FontSize.REGULAR)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = option.name, fontWeight = FontWeight.ExtraBold, fontSize = FontSize.REGULAR, color = colors.onSurface)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "${option.values.size} مقدار", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.sale.copy(alpha = 0.1f))
                    .clickable { onDeleteType() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp), tint = colors.sale)
            }
        }
        if (option.values.isNotEmpty()) {
            Spacer(modifier = Modifier.height(13.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                option.values.forEach { value ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceVariant)
                            .border(1.dp, colors.line, RoundedCornerShape(10.dp))
                            .padding(horizontal = 11.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(value.value, fontSize = FontSize.SMALL, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp).clickable { onDeleteValue(value.id) },
                            tint = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(13.dp))
        InlineAddRow(
            value = valInput,
            onValueChange = { valInput = it },
            placeholder = "افزودن مقدار جدید",
            buttonLabel = "افزودن",
            onSubmit = {
                if (valInput.isNotBlank()) {
                    onAddValue(valInput.trim())
                    valInput = ""
                }
            }
        )
    }
}

/** ردیفِ افزودنِ اینلاین: فیلد + دکمه (مطابق اسپک). */
@Composable
private fun InlineAddRow(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    buttonLabel: String,
    onSubmit: () -> Unit
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(placeholder, fontSize = FontSize.SMALL, color = colors.onSurfaceVariant) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceVariant,
                unfocusedContainerColor = colors.surfaceVariant,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.line,
                cursorColor = colors.primary,
                focusedTextColor = colors.onSurface,
                unfocusedTextColor = colors.onSurface
            )
        )
        Box(
            modifier = Modifier
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (value.isNotBlank()) colors.primary else colors.line)
                .clickable(enabled = value.isNotBlank()) { onSubmit() }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(buttonLabel, color = colors.onPrimary, fontSize = FontSize.SMALL, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}
