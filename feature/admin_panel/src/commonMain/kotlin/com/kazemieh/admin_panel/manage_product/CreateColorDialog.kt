package com.kazemieh.admin_panel.manage_product

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.Resources
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateColorDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, hex: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var hex by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Resources.String.CreateColor)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Resources.String.Name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = hex,
                    onValueChange = { hex = it },
                    label = { Text(stringResource(Resources.String.HexCodeOptional)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(Resources.String.HexCodePlaceholder)) }
                )
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name, hex.takeIf { it.isNotBlank() }) }
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
