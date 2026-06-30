package com.kazemieh.admin.options

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.admin.AdminOption
import com.kazemieh.domain.admin.AdminOptionValue
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageOptionsScreen(
    onBackClick: () -> Unit
) {
    val viewModel = koinViewModel<ManageOptionsViewModel>()
    val state by viewModel.state.collectAsState()
    val messageBarState = rememberMessageBarState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    var showAddTypeDialog by remember { mutableStateOf(false) }
    var typeToEdit by remember { mutableStateOf<AdminOption?>(null) }
    var valueToEdit by remember { mutableStateOf<Pair<Long, AdminOptionValue>?>(null) } // typeId to value
    var showAddValueDialogForType by remember { mutableStateOf<Long?>(null) }

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
            TopAppBar(
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
                actions = {
                    IconButton(onClick = { showAddTypeDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.options) { option ->
                        OptionTypeItem(
                            option = option,
                            onEditType = { typeToEdit = option },
                            onDeleteType = { viewModel.handleIntent(ManageOptionsIntent.DeleteOptionType(option.id)) },
                            onAddValue = { showAddValueDialogForType = option.id },
                            onEditValue = { value -> valueToEdit = option.id to value },
                            onDeleteValue = { valueId -> viewModel.handleIntent(ManageOptionsIntent.DeleteOptionValue(valueId)) }
                        )
                    }
                }
            }
        }
    }

    if (showAddTypeDialog) {
        OptionNameDialog(
            title = stringResource(Resources.String.AddOptionType),
            onDismiss = { showAddTypeDialog = false },
            onConfirm = { name ->
                viewModel.handleIntent(ManageOptionsIntent.CreateOptionType(name))
                showAddTypeDialog = false
            }
        )
    }

    typeToEdit?.let { type ->
        OptionNameDialog(
            title = stringResource(Resources.String.EditOptionType),
            initialValue = type.name,
            onDismiss = { typeToEdit = null },
            onConfirm = { name ->
                viewModel.handleIntent(ManageOptionsIntent.UpdateOptionType(type.id, name))
                typeToEdit = null
            }
        )
    }

    showAddValueDialogForType?.let { typeId ->
        OptionNameDialog(
            title = stringResource(Resources.String.AddOptionValue),
            onDismiss = { showAddValueDialogForType = null },
            onConfirm = { value ->
                viewModel.handleIntent(ManageOptionsIntent.CreateOptionValue(typeId, value))
                showAddValueDialogForType = null
            }
        )
    }

    valueToEdit?.let { (typeId, value) ->
        OptionNameDialog(
            title = stringResource(Resources.String.EditOptionValue),
            initialValue = value.value,
            onDismiss = { valueToEdit = null },
            onConfirm = { newValue ->
                viewModel.handleIntent(ManageOptionsIntent.UpdateOptionValue(value.id, typeId, newValue))
                valueToEdit = null
            }
        )
    }
}

@Composable
fun OptionTypeItem(
    option: AdminOption,
    onEditType: () -> Unit,
    onDeleteType: () -> Unit,
    onAddValue: () -> Unit,
    onEditValue: (AdminOptionValue) -> Unit,
    onDeleteValue: (Long) -> Unit
) {
    val colors = AppTheme.colors
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
            IconButton(onClick = onEditType) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.onSurfaceVariant)
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
                        .clickable { onEditValue(value) }
                        .padding(horizontal = 11.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(value.value, fontSize = FontSize.SMALL, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp).clickable { onDeleteValue(value.id) },
                        tint = colors.onSurfaceVariant
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.accentSoft)
                    .clickable { onAddValue() }
                    .padding(horizontal = 11.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.primary)
            }
        }
    }
}

@Composable
fun OptionNameDialog(
    title: String,
    initialValue: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Resources.String.Name)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name) }
            ) {
                Text(stringResource(Resources.String.Confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Resources.String.Cancel))
            }
        }
    )
}
