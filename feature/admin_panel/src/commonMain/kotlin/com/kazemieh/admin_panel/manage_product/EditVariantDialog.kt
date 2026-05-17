package com.kazemieh.admin_panel.manage_product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kazemieh.domain.model.admin.AdminVariant
import com.kazemieh.domain.repository.Color
import com.kazemieh.domain.repository.Size

@Composable
fun EditVariantDialog(
    variant: AdminVariant,
    sizes: List<Size>,
    colors: List<Color>,
    onDismiss: () -> Unit,
    onConfirm: (sku: String?, price: Double?, sizeId: Long?, colorId: Long?, isActive: Boolean?) -> Unit,
    onDelete: () -> Unit,
    onCreateSize: (name: String, sortOrder: Int) -> Unit,
    onCreateColor: (name: String, hex: String?) -> Unit
) {
    var sku by remember { mutableStateOf(variant.sku) }
    var price by remember { mutableStateOf(variant.price.toString()) }
    var isActive by remember { mutableStateOf(variant.isActive) }
    var selectedSize by remember { mutableStateOf(sizes.find { it.id == variant.sizeId }) }
    var selectedColor by remember { mutableStateOf(colors.find { it.id == variant.colorId }) }

    var showSizesBottomSheet by remember { mutableStateOf(false) }
    var showColorsBottomSheet by remember { mutableStateOf(false) }
    var showCreateSizeDialog by remember { mutableStateOf(false) }
    var showCreateColorDialog by remember { mutableStateOf(false) }

    if (showSizesBottomSheet) {
        SizesBottomSheet(
            sizes = sizes,
            onSizeSelected = { selectedSize = it },
            onCreateSizeClick = {
                showCreateSizeDialog = true
                showSizesBottomSheet = false
            },
            onDeleteSize = {},
            onDismiss = { showSizesBottomSheet = false }
        )
    }

    if (showCreateSizeDialog) {
        CreateSizeDialog(
            onDismiss = { showCreateSizeDialog = false },
            onConfirm = { name, sortOrder ->
                onCreateSize(name, sortOrder)
                showCreateSizeDialog = false
            }
        )
    }

    if (showColorsBottomSheet) {
        ColorsBottomSheet(
            colors = colors,
            onColorSelected = { selectedColor = it },
            onCreateColorClick = {
                showCreateColorDialog = true
                showColorsBottomSheet = false
            },
            onDeleteColor = {},
            onDismiss = { showColorsBottomSheet = false }
        )
    }

    if (showCreateColorDialog) {
        CreateColorDialog(
            onDismiss = { showCreateColorDialog = false },
            onConfirm = { name, hex ->
                onCreateColor(name, hex)
                showCreateColorDialog = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Variant") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Size Selection
                OutlinedCard(
                    onClick = { showSizesBottomSheet = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedSize?.name ?: "Select Size",
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Color Selection
                OutlinedCard(
                    onClick = { showColorsBottomSheet = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedColor?.name ?: "Select Color",
                        modifier = Modifier.padding(16.dp)
                    )
                }

                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("SKU") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Active")
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
                
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Variant")
                }
            }
        },
        confirmButton = {
            Button(
                enabled = sku.isNotBlank() && price.toDoubleOrNull() != null,
                onClick = {
                    onConfirm(sku, price.toDouble(), selectedSize?.id, selectedColor?.id, isActive)
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
