package com.kazemieh.admin_panel.manage_product

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.Resources
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateCategoryDialog(
    parentId: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, slug: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var slug by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(stringResource(Resources.String.CreateCategory)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        if (slug.isEmpty()) slug = it.lowercase().replace(" ", "-")
                    },
                    label = { Text(stringResource(Resources.String.Name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = slug,
                    onValueChange = { slug = it },
                    label = { Text(stringResource(Resources.String.Slug)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && slug.isNotBlank(),
                onClick = { onConfirm(name, slug) }
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
