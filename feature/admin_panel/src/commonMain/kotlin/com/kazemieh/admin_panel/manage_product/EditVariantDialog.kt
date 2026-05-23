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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.Resources
import org.jetbrains.compose.resources.stringResource
import com.kazemieh.domain.model.admin.AdminVariant

@Composable
fun EditVariantDialog(
    variant: AdminVariant,
    onDismiss: () -> Unit,
    onConfirm: (sku: String?, price: Double?, options: Map<String, String>?, isActive: Boolean?) -> Unit,
    onDelete: () -> Unit
) {
    var sku by remember { mutableStateOf(variant.sku) }
    var price by remember { mutableStateOf(variant.price.toString()) }
    var isActive by remember { mutableStateOf(variant.isActive) }
    var options by remember { mutableStateOf(variant.options.toList().toMutableList()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Resources.String.EditVariant)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(Resources.String.Variants), style = MaterialTheme.typography.titleSmall)
                options.forEachIndexed { index, (key, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = key,
                            onValueChange = {
                                val newList = options.toMutableList()
                                newList[index] = it to value
                                options = newList
                            },
                            label = { Text(stringResource(Resources.String.Name)) }
                        )
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = value,
                            onValueChange = {
                                val newList = options.toMutableList()
                                newList[index] = key to it
                                options = newList
                            },
                            label = { Text(stringResource(Resources.String.Default)) }
                        )
                        IconButton(onClick = {
                            if (options.size > 1) {
                                val newList = options.toMutableList()
                                newList.removeAt(index)
                                options = newList
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                TextButton(
                    onClick = {
                        val newList = options.toMutableList()
                        newList.add("" to "")
                        options = newList
                    },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(Resources.String.Add))
                }

                HorizontalDivider()

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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(Resources.String.Active))
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
                
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(Resources.String.DeleteVariant))
                }
            }
        },
        confirmButton = {
            Button(
                enabled = options.all { it.first.isNotBlank() && it.second.isNotBlank() } && sku.isNotBlank() && price.toDoubleOrNull() != null,
                onClick = {
                    onConfirm(sku, price.toDouble(), options.toMap(), isActive)
                }
            ) {
                Text(stringResource(Resources.String.Update))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Resources.String.Cancel))
            }
        }
    )
}
