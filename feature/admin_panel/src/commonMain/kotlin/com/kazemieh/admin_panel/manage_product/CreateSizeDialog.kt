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

@Composable
fun CreateSizeDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, sortOrder: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Resources.String.CreateSize)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Resources.String.Name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sortOrder,
                    onValueChange = { sortOrder = it },
                    label = { Text(stringResource(Resources.String.SortOrder)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name, sortOrder.toIntOrNull() ?: 0) }
            ) {
                Text(stringResource(Resources.String.Create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Resources.String.Cancel))
            }
        }
    )
}
