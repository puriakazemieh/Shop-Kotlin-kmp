package com.kazemieh.admin_panel.manage_product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.Resources
import com.kazemieh.domain.repository.Size
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SizesBottomSheet(
    sizes: List<Size>,
    onSizeSelected: (Size) -> Unit,
    onCreateSizeClick: () -> Unit,
    onDeleteSize: (Long) -> Unit,
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
                        text = stringResource(Resources.String.SelectSize),
                        style = MaterialTheme.typography.titleLarge
                    )
                    TextButton(onClick = onCreateSizeClick) {
                        Text(stringResource(Resources.String.AddNew))
                    }
                }
            }
            items(sizes) { size ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = size.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { 
                                onSizeSelected(size)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp)
                    )
                    IconButton(onClick = { onDeleteSize(size.id) }) {
                        Icon(
                            painter = painterResource(Resources.Icon.Delete),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
