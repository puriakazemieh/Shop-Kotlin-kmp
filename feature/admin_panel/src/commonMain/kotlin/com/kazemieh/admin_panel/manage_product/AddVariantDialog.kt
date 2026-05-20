package com.kazemieh.admin_panel.manage_product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.Resources
import org.jetbrains.compose.resources.stringResource
import com.kazemieh.domain.repository.Color
import com.kazemieh.domain.repository.Size

@Composable
fun AddVariantDialog(
    sizes: List<Size>,
    colors: List<Color>,
    onDismiss: () -> Unit,
    onConfirm: (sizeId: Long, colorId: Long, sku: String, price: Double, initialOnHand: Int) -> Unit,
    onCreateSize: (name: String, sortOrder: Int) -> Unit,
    onDeleteSize: (Long) -> Unit,
    onCreateColor: (name: String, hex: String?) -> Unit,
    onDeleteColor: (Long) -> Unit
) {
    var selectedSize by remember { mutableStateOf<Size?>(null) }
    var selectedColor by remember { mutableStateOf<Color?>(null) }
    var sku by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var initialOnHand by remember { mutableStateOf("") }

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
            onDeleteSize = onDeleteSize,
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
            onDeleteColor = onDeleteColor,
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
        title = { Text(stringResource(Resources.String.AddNewVariant)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Size Selection
                OutlinedCard(
                    onClick = { showSizesBottomSheet = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedSize?.name ?: stringResource(Resources.String.SelectSize),
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Color Selection
                OutlinedCard(
                    onClick = { showColorsBottomSheet = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedColor?.name ?: stringResource(Resources.String.SelectColor),
                        modifier = Modifier.padding(16.dp)
                    )
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
                enabled = selectedSize != null && selectedColor != null && sku.isNotBlank() && price.toDoubleOrNull() != null,
                onClick = {
                    onConfirm(
                        selectedSize!!.id,
                        selectedColor!!.id,
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
