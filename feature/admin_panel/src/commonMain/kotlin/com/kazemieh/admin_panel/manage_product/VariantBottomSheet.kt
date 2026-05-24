package com.kazemieh.admin_panel.manage_product

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
    availableOptions: List<AdminOption>,
    onDismiss: () -> Unit,
    onConfirm: (sku: String, price: Double, options: List<AdminVariantOption>, isActive: Boolean, initialOnHand: Int) -> Unit,
    onCreateOptionType: (String) -> Unit,
    onCreateOptionValue: (Long, String) -> Unit,
    onCreateOptionTypeAndValue: (String, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var options by remember {
        mutableStateOf(
            variant?.options?.map { AdminVariantOption(it.key, it.value) } ?: listOf(
                AdminVariantOption("", "")
            )
        )
    }
    var sku by remember { mutableStateOf(variant?.sku ?: "") }
    var price by remember { mutableStateOf(variant?.price?.toString() ?: "") }
    var isActive by remember { mutableStateOf(variant?.isActive ?: true) }
    var initialOnHand by remember { mutableStateOf(variant?.onHand?.toString() ?: "") }


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
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
                        IconButton(onClick = {
                            if (options.size > 1) {
                                options = options.filterIndexed { i, _ -> i != index }
                            }
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Option",
                                tint = MaterialTheme.colorScheme.error
                            )
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

            TextButton(
                onClick = { options = options + AdminVariantOption("", "") },
                enabled = options.all { it.type.isNotBlank() && it.value.isNotBlank() } && options.map { it.type.lowercase() }.distinct().size == options.size,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Option")
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
                label = { Text("Initial Stock *") },
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
                                options,
                                isActive,
                                initialOnHand.toInt()
                            )
                            // Reset form for next variant
                            sku = ""
                            price = ""
                            initialOnHand = ""
                            options = listOf(AdminVariantOption("", ""))
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
                            options,
                            isActive,
                            initialOnHand.toInt()
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
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            value = value,
            onValueChange = {
                onValueChange(it)
            },
            label = { Text(label) },
            isError = isError,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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
