package com.kazemieh.admin.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.domain.model.admin.AdminOption
import com.kazemieh.domain.model.admin.AdminVariant
import com.kazemieh.domain.model.admin.AdminVariantOption
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VariantBottomSheet(
    variant: AdminVariant? = null,
    existingVariants: List<AdminVariant> = emptyList(),
    availableOptions: List<AdminOption>,
    defaultOptionTypes: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (sku: String, price: Double, options: List<AdminVariantOption>, isActive: Boolean, initialOnHand: Int, shouldDismiss: Boolean) -> Unit,
    onApplyToAll: (List<AdminVariantOption>) -> Unit,
    onCreateOptionType: (String) -> Unit,
    onCreateOptionValue: (Long, String) -> Unit,
    onCreateOptionTypeAndValue: (String, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var options by remember {
        val masterVariant = existingVariants.firstOrNull()
        val masterKeys = masterVariant?.options?.keys ?: emptySet()
        
        mutableStateOf(
            if (variant != null) {
                if (variant.id == masterVariant?.id) {
                    // It's the master variant, use its own options
                    variant.options.map { AdminVariantOption(it.key, it.value) }.toMutableList()
                } else {
                    // It's a subsequent variant, STRICTLY sync with master variant's keys
                    masterKeys.map { key ->
                        AdminVariantOption(key, variant.options[key] ?: "")
                    }.toMutableList()
                }
            } else if (defaultOptionTypes.isNotEmpty()) {
                defaultOptionTypes.map { AdminVariantOption(it, "") }
            } else {
                listOf(AdminVariantOption("", ""))
            }
        )
    }
    var sku by remember { mutableStateOf(variant?.sku ?: "") }
    var price by remember { mutableStateOf(variant?.price?.toString() ?: "") }
    var isActive by remember { mutableStateOf(variant?.isActive ?: true) }
    var initialOnHand by remember { mutableStateOf(variant?.onHand?.toString() ?: "") }

    val isMasterVariant = variant != null && existingVariants.firstOrNull()?.id == variant.id
    val isSubsequentVariant = (defaultOptionTypes.isNotEmpty() && variant == null) || (variant != null && !isMasterVariant)

    var showDeletePropertyWarning by remember { mutableStateOf<Int?>(null) }
    var causesDuplicates by remember { mutableStateOf(false) }

    if (showDeletePropertyWarning != null) {
        AlertDialog(
            onDismissRequest = { showDeletePropertyWarning = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(stringResource(Resources.String.WarningText)) },
            text = { 
                Text(
                    if (causesDuplicates) stringResource(Resources.String.DeletePropertyDuplicateWarning)
                    else stringResource(Resources.String.DeletePropertyWarning)
                ) 
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeletePropertyWarning?.let { index ->
                        options = options.filterIndexed { i, _ -> i != index }
                    }
                    showDeletePropertyWarning = null
                }) {
                    Text(stringResource(Resources.String.DeleteAnyway), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePropertyWarning = null }) {
                    Text(stringResource(Resources.String.Cancel))
                }
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
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (variant == null) stringResource(Resources.String.AddNewVariant) else stringResource(Resources.String.EditVariant),
                fontSize = FontSize.LARGE,
                fontWeight = FontWeight.Bold
            )

            options.forEachIndexed { index, option ->
                val otherSelectedTypes = options.filterIndexed { i, _ -> i != index }.map { it.type.trim().lowercase() }
                val isDuplicateType = option.type.trim().lowercase() in otherSelectedTypes && option.type.isNotBlank()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            OptionSelector(
                                label = stringResource(Resources.String.OptionType),
                                value = option.type,
                                suggestions = availableOptions.map { it.name }.filter { it.lowercase() !in otherSelectedTypes },
                                isError = isDuplicateType,
                                enabled = !isSubsequentVariant,
                                onValueChange = { newType ->
                                    val newList = options.toMutableList()
                                    newList[index] = option.copy(type = newType, value = "")
                                    options = newList
                                }
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            OptionSelector(
                                label = stringResource(Resources.String.OptionValue),
                                value = option.value,
                                suggestions = availableOptions.find { it.name == option.type }?.values?.map { it.value }
                                    ?: emptyList(),
                                onValueChange = { newValue ->
                                    val newList = options.toMutableList()
                                    newList[index] = option.copy(value = newValue)
                                    options = newList
                                }
                            )
                        }
                        if (!isSubsequentVariant) {
                            IconButton(onClick = {
                                if (options.size > 1) {
                                    if (isMasterVariant) {
                                        val remainingKeys = options.filterIndexed { i, _ -> i != index }.map { it.type }
                                        val futureVariants = existingVariants.map { v -> 
                                            v.options.filterKeys { it in remainingKeys } 
                                        }
                                        causesDuplicates = futureVariants.size != futureVariants.distinct().size
                                        showDeletePropertyWarning = index
                                    } else {
                                        options = options.filterIndexed { i, _ -> i != index }
                                    }
                                }
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(Resources.String.DeleteOption),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    if (isDuplicateType) {
                        Text(
                            text = stringResource(Resources.String.DuplicateOptionType),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = FontSize.EXTRA_SMALL,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // Clear actions for new properties
                    val typeExists = availableOptions.any { it.name == option.type }
                    val valueExists = availableOptions.find { it.name == option.type }?.values?.any { it.value == option.value } ?: false

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (option.type.isNotBlank() && !typeExists && option.value.isNotBlank() && !isDuplicateType) {
                            TextButton(
                                onClick = { onCreateOptionTypeAndValue(option.type, option.value) },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = stringResource(Resources.String.CreateTypeAndValue),
                                    fontSize = FontSize.SMALL
                                )
                            }
                        } else {
                            if (option.type.isNotBlank() && !typeExists && !isDuplicateType) {
                                TextButton(
                                    onClick = { onCreateOptionType(option.type) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(Resources.String.CreateTypeFormat, option.type),
                                        fontSize = FontSize.SMALL
                                    )
                                }
                            }
                            if (option.value.isNotBlank() && typeExists && !valueExists && !isDuplicateType) {
                                TextButton(
                                    onClick = {
                                        availableOptions.find { it.name == option.type }?.id?.let { id ->
                                            onCreateOptionValue(id, option.value)
                                        }
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(Resources.String.CreateValueFormat, option.value),
                                        fontSize = FontSize.SMALL
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (!isSubsequentVariant) {
                TextButton(
                    onClick = { options = options + AdminVariantOption("", "") },
                    enabled = options.all { it.type.isNotBlank() && it.value.isNotBlank() } && options.map { it.type.lowercase() }.distinct().size == options.size,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Resources.String.AddVariant))
                }
            }

            val currentOptionsMap = options.associate { it.type.trim() to it.value.trim() }
            val isDuplicateVariant = existingVariants.any {
                it.id != variant?.id && it.options.mapValues { e -> e.value.trim() } == currentOptionsMap
            }

            if (isDuplicateVariant) {
                Text(
                    text = stringResource(Resources.String.VariantComboExists),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = FontSize.SMALL,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Apply to all variants logic
            val masterKeysInOtherVariants = existingVariants.getOrNull(1)?.options?.keys ?: emptySet()
            val newCompletedProperties = options.filter { 
                it.type.isNotBlank() && it.value.isNotBlank() && it.type !in masterKeysInOtherVariants 
            }
            val hasNewCompletedProperties = isMasterVariant && existingVariants.size > 1 && newCompletedProperties.isNotEmpty()

            if (hasNewCompletedProperties) {
                Button(
                    onClick = {
                        onApplyToAll(options.filter { it.type.isNotBlank() && it.value.isNotBlank() })
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Resources.String.ApplyToAll))
                }
            }

            OutlinedTextField(
                value = sku,
                onValueChange = { sku = it },
                label = { Text(stringResource(Resources.String.Sku)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text(stringResource(Resources.String.Price)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = initialOnHand,
                onValueChange = { initialOnHand = it },
                label = { Text(stringResource(Resources.String.InitialStockRequired)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = initialOnHand.isEmpty()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Resources.String.Active), fontSize = FontSize.REGULAR)
                Switch(checked = isActive, onCheckedChange = { isActive = it })
            }

            if (onDelete != null) {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Resources.String.DeleteVariant))
                }
            }

            val isFormValid = options.all { it.type.isNotBlank() && it.value.isNotBlank() } &&
                    options.map { it.type.trim().lowercase() }.distinct().size == options.size &&
                    !isDuplicateVariant &&
                    sku.isNotBlank() &&
                    price.toDoubleOrNull() != null &&
                    initialOnHand.toIntOrNull() != null

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (variant == null) {
                    OutlinedButton(
                        enabled = isFormValid,
                        onClick = {
                            onConfirm(
                                sku,
                                price.toDouble(),
                                options.filter { it.type.isNotBlank() && it.value.isNotBlank() },
                                isActive,
                                initialOnHand.toInt(),
                                false
                            )
                            // Reset form for next variant while keeping the same property types
                            sku = ""
                            price = ""
                            initialOnHand = ""
                            options = options.map { it.copy(value = "") }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(Resources.String.AddAndNext))
                    }
                }

                Button(
                    enabled = isFormValid,
                    onClick = {
                        onConfirm(
                            sku,
                            price.toDouble(),
                            options.filter { it.type.isNotBlank() && it.value.isNotBlank() },
                            isActive,
                            initialOnHand.toInt(),
                            true
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (variant == null) stringResource(Resources.String.Add) else stringResource(Resources.String.Update))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionSelector(
    label: String,
    value: String,
    suggestions: List<String>,
    isError: Boolean = false,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            value = value,
            onValueChange = {
                if (enabled) onValueChange(it)
            },
            readOnly = !enabled,
            enabled = enabled,
            label = { Text(label) },
            isError = isError,
            trailingIcon = { if (enabled) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        if (suggestions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            onValueChange(suggestion)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
