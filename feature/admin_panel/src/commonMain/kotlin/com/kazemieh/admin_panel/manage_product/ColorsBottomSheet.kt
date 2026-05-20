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
import com.kazemieh.domain.repository.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorsBottomSheet(
    colors: List<Color>,
    onColorSelected: (Color) -> Unit,
    onCreateColorClick: () -> Unit,
    onDeleteColor: (Long) -> Unit,
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
                        text = stringResource(Resources.String.SelectColor),
                        style = MaterialTheme.typography.titleLarge
                    )
                    TextButton(onClick = onCreateColorClick) {
                        Text(stringResource(Resources.String.AddNew))
                    }
                }
            }
            items(colors) { color ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = color.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { 
                                onColorSelected(color)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp)
                    )
                    IconButton(onClick = { onDeleteColor(color.id) }) {
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
