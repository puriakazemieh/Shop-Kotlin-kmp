package com.kazemieh.admin_panel.manage_product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.Resources
import com.kazemieh.domain.model.admin.AdminOption
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVariantDialog(
    availableOptions: List<AdminOption>,
    onDismiss: () -> Unit,
    onConfirm: (optionType: String, optionValue: String, sku: String, price: Double, initialOnHand: Int) -> Unit
) {
    var optionType by remember { mutableStateOf("") }
    var optionValue by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var initialOnHand by remember { mutableStateOf("") }

    var typeExpanded by remember { mutableStateOf(false) }
    var valueExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Resources.String.AddNewVariant)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor(),
                        value = optionType,
                        onValueChange = { optionType = it },
                        label = { Text("Option Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        availableOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name) },
                                onClick = {
                                    optionType = option.name
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = valueExpanded,
                    onExpandedChange = { valueExpanded = it }
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor(),
                        value = optionValue,
                        onValueChange = { optionValue = it },
                        label = { Text("Option Value") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = valueExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = valueExpanded,
                        onDismissRequest = { valueExpanded = false }
                    ) {
                        availableOptions.find { it.name == optionType }?.values?.forEach { value ->
                            DropdownMenuItem(
                                text = { Text(value.value) },
                                onClick = {
                                    optionValue = value.value
                                    valueExpanded = false
                                }
                            )
                        }
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
                    label = { Text(stringResource(Resources.String.InitialStock)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                enabled = optionType.isNotBlank() && optionValue.isNotBlank() && sku.isNotBlank() && price.toDoubleOrNull() != null,
                onClick = {
                    onConfirm(
                        optionType,
                        optionValue,
                        sku,
                        price.toDouble(),
                        initialOnHand.toIntOrNull() ?: 0
                    )
                }
            ) {
                Text(stringResource(Resources.String.Add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Resources.String.Cancel))
            }
        }
    )
}
