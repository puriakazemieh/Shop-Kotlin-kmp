package com.kazemieh.admin_panel.manage_product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kazemieh.domain.model.admin.AdminVariant

@Composable
fun EditVariantDialog(
    variant: AdminVariant,
    onDismiss: () -> Unit,
    onConfirm: (sku: String?, price: Double?, isActive: Boolean?) -> Unit,
    onDelete: () -> Unit
) {
    var sku by remember { mutableStateOf(variant.sku) }
    var price by remember { mutableStateOf(variant.price.toString()) }
    var isActive by remember { mutableStateOf(variant.isActive) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Variant") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${variant.sizeName} / ${variant.colorName}", style = MaterialTheme.typography.labelLarge)
                
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
                    onConfirm(sku, price.toDouble(), isActive)
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
