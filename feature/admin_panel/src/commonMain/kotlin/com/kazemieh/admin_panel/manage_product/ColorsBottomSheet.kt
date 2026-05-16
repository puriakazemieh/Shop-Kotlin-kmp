package com.kazemieh.admin_panel.manage_product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.domain.repository.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorsBottomSheet(
    colors: List<Color>,
    onColorSelected: (Color) -> Unit,
    onCreateColorClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Color",
                        style = MaterialTheme.typography.titleLarge
                    )
                    TextButton(onClick = onCreateColorClick) {
                        Text("Add New")
                    }
                }
            }
            items(colors) { color ->
                Text(
                    text = color.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            onColorSelected(color)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
