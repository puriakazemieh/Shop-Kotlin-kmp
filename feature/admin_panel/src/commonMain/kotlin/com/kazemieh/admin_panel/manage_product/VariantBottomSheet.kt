package com.kazemieh.admin_panel.manage_product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VariantBottomSheet(
    variant: AdminVariant? = null,
    availableOptions: List<AdminOption>,
    onDismiss: () -> Unit,
    onConfirm: (sku: String, price: Double, optionType: String, optionValue: String, isActive: Boolean, initialOnHand: Int) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var optionType by remember { mutableStateOf(variant?.options?.keys?.firstOrNull() ?: "") }
    var optionValue by remember { mutableStateOf(variant?.options?.values?.firstOrNull() ?: "") }
    var sku by remember { mutableStateOf(variant?.sku ?: "") }
    var price by remember { mutableStateOf(variant?.price?.toString() ?: "") }
    var isActive by remember { mutableStateOf(variant?.isActive ?: true) }
    var initialOnHand by remember { mutableStateOf(variant?.onHand?.toString() ?: "") }

    var typeExpanded by remember { mutableStateOf(false) }
    var valueExpanded by remember { mutableStateOf(false) }

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

            // Option Type
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it }
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    value = optionType,
                    onValueChange = { 
                        optionType = it
                        optionValue = "" // Reset value when type changes
                    },
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
                                optionValue = "" // Reset value
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            // Option Value
            ExposedDropdownMenuBox(
                expanded = valueExpanded,
                onExpandedChange = { valueExpanded = it }
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
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

            Button(
                enabled = optionType.isNotBlank() && 
                          optionValue.isNotBlank() && 
                          sku.isNotBlank() && 
                          price.toDoubleOrNull() != null &&
                          initialOnHand.toIntOrNull() != null,
                onClick = {
                    onConfirm(
                        sku,
                        price.toDouble(),
                        optionType,
                        optionValue,
                        isActive,
                        initialOnHand.toInt()
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (variant == null) stringResource(Resources.String.Add) else stringResource(Resources.String.Update))
            }
        }
    }
}
