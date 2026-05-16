package com.kazemieh.admin_panel.manage_product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.domain.model.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesBottomSheet(
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit,
    onCreateCategoryClick: () -> Unit,
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
                        text = "Select Category",
                        style = MaterialTheme.typography.titleLarge
                    )
                    TextButton(onClick = onCreateCategoryClick) {
                        Text("Add New")
                    }
                }
            }
            items(categories) { category ->
                CategoryItem(
                    category = category,
                    onSelected = {
                        onCategorySelected(it)
                        onDismiss()
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onSelected: (Category) -> Unit,
    level: Int = 0
) {
    Column {
        Text(
            text = category.name,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelected(category) }
                .padding(vertical = 12.dp, horizontal = (level * 16).dp),
            style = MaterialTheme.typography.bodyLarge
        )
        // If children are needed in the UI, they can be rendered recursively here
        // For now, I'll keep it simple as a flat list if the server returns flat or just top level
    }
}
