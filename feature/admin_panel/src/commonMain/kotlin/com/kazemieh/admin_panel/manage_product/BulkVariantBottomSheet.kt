package com.kazemieh.admin_panel.manage_product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.domain.model.admin.AdminOption
import com.kazemieh.domain.model.admin.AdminVariantOption
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BulkVariantBottomSheet(
    availableOptions: List<AdminOption>,
    onDismiss: () -> Unit,
    onGenerate: (List<List<AdminVariantOption>>) -> Unit,
    onCreateOptionType: (String) -> Unit,
    onCreateOptionValue: (Long, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var selectedOptions by remember { mutableStateOf(mapOf<Long, Set<Long>>()) }
    var showAddTypeDialog by remember { mutableStateOf(false) }
    var showAddValueToTypeId by remember { mutableStateOf<Long?>(null) }

    if (showAddTypeDialog) {
        var newTypeName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddTypeDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(stringResource(Resources.String.CreateCategory)) }, // Reuse or add new string
            text = {
                OutlinedTextField(
                    value = newTypeName,
                    onValueChange = { newTypeName = it },
                    label = { Text(stringResource(Resources.String.Name)) }
                )
            },
            confirmButton = {
                Button(onClick = {
                    onCreateOptionType(newTypeName)
                    showAddTypeDialog = false
                }) { Text(stringResource(Resources.String.Add)) }
            }
        )
    }

    if (showAddValueToTypeId != null) {
        var newValueName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddValueToTypeId = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(stringResource(Resources.String.Add)) },
            text = {
                OutlinedTextField(
                    value = newValueName,
                    onValueChange = { newValueName = it },
                    label = { Text(stringResource(Resources.String.Name)) }
                )
            },
            confirmButton = {
                Button(onClick = {
                    onCreateOptionValue(showAddValueToTypeId!!, newValueName)
                    showAddValueToTypeId = null
                }) { Text(stringResource(Resources.String.Add)) }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Resources.String.BulkAddVariants),
                        fontSize = FontSize.LARGE,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(Resources.String.SelectAttributesToCombine),
                        fontSize = FontSize.SMALL,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { showAddTypeDialog = true }) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Default.Add, contentDescription = stringResource(Resources.String.AddProperty))
                }
            }

            availableOptions.forEach { option ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = FontSize.MEDIUM
                        )
                        TextButton(onClick = { showAddValueToTypeId = option.id }) {
                            Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(stringResource(Resources.String.Add), fontSize = FontSize.EXTRA_SMALL)
                        }
                    }
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        option.values.forEach { valObj ->
                            val isSelected = selectedOptions[option.id]?.contains(valObj.id) == true
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val currentSet = selectedOptions[option.id] ?: emptySet()
                                    val newSet = if (isSelected) currentSet - valObj.id else currentSet + valObj.id
                                    selectedOptions = selectedOptions + (option.id to newSet)
                                },
                                label = { Text(valObj.value) }
                            )
                        }
                    }
                    Divider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }

            val totalCombinations = if (selectedOptions.isEmpty()) 0 
                                   else selectedOptions.values.filter { it.isNotEmpty() }
                                       .fold(1) { acc, set -> acc * set.size }

            Button(
                enabled = totalCombinations > 0,
                onClick = {
                    val result = generateCombinations(availableOptions, selectedOptions)
                    onGenerate(result)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (totalCombinations > 0) 
                        stringResource(Resources.String.VariantsGeneratedCount, totalCombinations.toString())
                    else 
                        stringResource(Resources.String.Generate)
                )
            }
        }
    }
}

private fun generateCombinations(
    availableOptions: List<AdminOption>,
    selectedOptions: Map<Long, Set<Long>>
): List<List<AdminVariantOption>> {
    val activeOptions = availableOptions.filter { selectedOptions[it.id]?.isNotEmpty() == true }
    if (activeOptions.isEmpty()) return emptyList()

    var combinations = listOf(emptyList<AdminVariantOption>())

    activeOptions.forEach { option ->
        val selectedValues = option.values.filter { it.id in (selectedOptions[option.id] ?: emptySet()) }
        val nextCombinations = mutableListOf<List<AdminVariantOption>>()
        
        combinations.forEach { existingCombo ->
            selectedValues.forEach { valueObj ->
                nextCombinations.add(existingCombo + AdminVariantOption(option.name, valueObj.value))
            }
        }
        combinations = nextCombinations
    }

    return combinations
}
