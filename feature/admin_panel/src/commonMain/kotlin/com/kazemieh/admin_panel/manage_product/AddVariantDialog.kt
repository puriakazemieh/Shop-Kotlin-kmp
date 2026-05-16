package com.kazemieh.admin_panel.manage_product

import androidx.compose.foundation.layout.*
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
    onDismiss: () -> Unit,
    onConfirm: (sku: String, price: Double, initialOnHand: Int) -> Unit
) {
    var sku by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var initialOnHand by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Variant") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                enabled = sku.isNotBlank() && price.toDoubleOrNull() != null,
                onClick = {
                    onConfirm(
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
