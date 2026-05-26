package com.kazemieh.admin.options

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.model.admin.AdminOption
import com.kazemieh.domain.model.admin.AdminOptionValue
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = option.name, fontWeight = FontWeight.Bold, fontSize = FontSize.MEDIUM)
                Row {
                    IconButton(onClick = onEditType) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDeleteType) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                option.values.forEach { value ->
                    Surface(
                        modifier = Modifier.clip(MaterialTheme.shapes.small).clickable { onEditValue(value) },
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(value.value, fontSize = FontSize.SMALL)
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp).clickable { onDeleteValue(value.id) },
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                IconButton(onClick = onAddValue) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
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
