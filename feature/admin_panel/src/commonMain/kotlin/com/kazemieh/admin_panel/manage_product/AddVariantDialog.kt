package com.kazemieh.admin_panel.manage_product

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kazemieh.domain.repository.Color
import com.kazemieh.domain.repository.Size

@Composable
fun AddVariantDialog(
    sizes: List<Size>,
    colors: List<Color>,
    onDismiss: () -> Unit,
    onConfirm: (sizeId: Long, colorId: Long, sku: String, price: Double, initialOnHand: Int) -> Unit
) {
    var selectedSize by remember { mutableStateOf<Size?>(null) }
    var selectedColor by remember { mutableStateOf<Color?>(null) }
    var sku by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var initialOnHand by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Variant") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Size Selection
                Text("Size", style = MaterialTheme.typography.labelMedium)
                ScrollableRow {
                    sizes.forEach { size ->
                        FilterChip(
                            selected = selectedSize?.id == size.id,
                            onClick = { selectedSize = size },
                            label = { Text(size.name) }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                // Color Selection
                Text("Color", style = MaterialTheme.typography.labelMedium)
                ScrollableRow {
                    colors.forEach { color ->
                        FilterChip(
                            selected = selectedColor?.id == color.id,
                            onClick = { selectedColor = color },
                            label = { Text(color.name) }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
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
                OutlinedTextField(
                    value = initialOnHand,
                    onValueChange = { initialOnHand = it },
                    label = { Text("Initial Stock") },
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
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ScrollableRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        content = { content() }
    )
}
