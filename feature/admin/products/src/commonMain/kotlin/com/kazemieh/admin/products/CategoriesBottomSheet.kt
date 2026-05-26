package com.kazemieh.admin.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.Resources
import com.kazemieh.domain.model.Category
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesBottomSheet(
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit,
    onCreateCategoryClick: () -> Unit,
    onDeleteCategory: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
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
                        text = stringResource(Resources.String.SelectCategory),
                        style = MaterialTheme.typography.titleLarge
                    )
                    TextButton(onClick = onCreateCategoryClick) {
                        Text(stringResource(Resources.String.AddNew))
                    }
                }
            }
            items(categories) { category ->
                CategoryItem(
                    category = category,
                    onSelected = {
                        onCategorySelected(it)
                        onDismiss()
                    },
                    onDelete = onDeleteCategory
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
    onDelete: (Long) -> Unit,
    level: Int = 0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected(category) }
            .padding(vertical = 8.dp, horizontal = (level * 16).dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { onDelete(category.id) }) {
            Icon(
                painter = painterResource(Resources.Icon.Delete),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
