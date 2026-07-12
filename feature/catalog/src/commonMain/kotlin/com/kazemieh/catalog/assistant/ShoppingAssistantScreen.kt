package com.kazemieh.catalog.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.util.formatToman
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingAssistantScreen(
    onBackClick: () -> Unit,
    navigateToDetails: (String) -> Unit
) {
    val viewModel = koinViewModel<ShoppingAssistantViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    var input by remember { mutableStateOf("") }

    Scaffold(
        containerColor = colors.surface,
        topBar = {
            TopAppBar(
                title = { Text("دستیارِ خرید", color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface, titleContentColor = colors.onSurface)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(colors.surface).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("سؤالت رو بپرس یا دنبالِ محصولی بگرد...", fontSize = FontSize.SMALL) },
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "ارسال",
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = FontSize.SMALL,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = input.isNotBlank()) {
                            viewModel.send(input)
                            input = ""
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().responsiveMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.messages) { message ->
                AssistantBubble(message, onProductClick = navigateToDetails)
            }
            if (state.isSearching) {
                item {
                    Text("در حالِ جست‌وجو...", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                }
            }
        }
    }
}

@Composable
private fun AssistantBubble(message: AssistantMessage, onProductClick: (String) -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.fromUser) Alignment.End else Alignment.Start
    ) {
        Text(
            message.text,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (message.fromUser) colors.primary else colors.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            color = if (message.fromUser) colors.onPrimary else colors.onSurface,
            fontSize = FontSize.SMALL
        )
        if (message.products.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(message.products) { product ->
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface)
                            .clickable { onProductClick(product.slug) }
                            .padding(10.dp)
                    ) {
                        Text(product.title, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${formatToman(product.minDiscountedPrice ?: product.minPrice)} تومان",
                            fontSize = FontSize.EXTRA_SMALL, color = colors.primary
                        )
                    }
                }
            }
        }
    }
}
