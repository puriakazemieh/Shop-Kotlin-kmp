package com.kazemieh.admin_panel.manage_product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.domain.repository.Size

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SizesBottomSheet(
    sizes: List<Size>,
    onSizeSelected: (Size) -> Unit,
    onCreateSizeClick: () -> Unit,
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
                        text = "Select Size",
                        style = MaterialTheme.typography.titleLarge
                    )
                    TextButton(onClick = onCreateSizeClick) {
                        Text("Add New")
                    }
                }
            }
            items(sizes) { size ->
                Text(
                    text = size.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            onSizeSelected(size)
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
