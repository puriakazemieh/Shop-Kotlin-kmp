package com.kazemieh.admin.products

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.domain.admin.AdminInteraction
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInteractionsScreen(
    navigateBack: () -> Unit
) {
    val viewModel = koinViewModel<AdminInteractionsViewModel>()
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مدیریت نظرات و پرسش‌ها") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Text("←") // Or a proper icon
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = state.selectedTab) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.handleIntent(AdminInteractionsIntent.SelectTab(0)) },
                    text = { Text("نظرات") }
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.handleIntent(AdminInteractionsIntent.SelectTab(1)) },
                    text = { Text("پرسش‌ها") }
                )
            }

            FilterSection(
                isNewFilter = state.isNewFilter,
                onNewFilterChange = { viewModel.handleIntent(AdminInteractionsIntent.SetNewFilter(it)) }
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val list = if (state.selectedTab == 0) state.reviews else state.questions
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(list) { interaction ->
                        AdminInteractionItem(interaction)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSection(
    isNewFilter: Boolean?,
    onNewFilterChange: (Boolean?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.FilterList, contentDescription = null)
        FilterChip(
            selected = isNewFilter == null,
            onClick = { onNewFilterChange(null) },
            label = { Text("همه") }
        )
        FilterChip(
            selected = isNewFilter == true,
            onClick = { onNewFilterChange(true) },
            label = { Text("جدید") }
        )
        FilterChip(
            selected = isNewFilter == false,
            onClick = { onNewFilterChange(false) },
            label = { Text("بررسی شده") }
        )
    }
}

@Composable
fun AdminInteractionItem(interaction: AdminInteraction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (interaction.isNew) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = interaction.userName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (interaction.isNew) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "جدید",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = interaction.createdAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "محصول: ${interaction.productTitle}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )

            interaction.rating?.let {
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    repeat(5) { index ->
                        Text(
                            text = if (index < it) "★" else "☆",
                            color = if (index < it) AppTheme.colors.star else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Text(
                text = interaction.content,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
