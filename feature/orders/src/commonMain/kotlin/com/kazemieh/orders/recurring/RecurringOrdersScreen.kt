package com.kazemieh.orders.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.domain.order.RecurringOrder
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringOrdersScreen(onBackClick: () -> Unit) {
    val viewModel = koinViewModel<RecurringOrdersViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        containerColor = colors.surface,
        topBar = {
            TopAppBar(
                title = { Text("خریدهایِ تکراری", color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface, titleContentColor = colors.onSurface)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val result = state.listState) {
                is AppResult.Loading -> LoadingCard(modifier = Modifier.fillMaxSize())
                is AppResult.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(result.message.toString(), color = colors.sale)
                }
                is AppResult.Success -> {
                    if (result.data.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("هنوز خریدِ تکراری‌ای ثبت نکرده‌اید.", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(result.data) { ro ->
                                RecurringOrderRow(ro, onCancel = { viewModel.cancel(ro.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecurringOrderRow(ro: RecurringOrder, onCancel: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("واریانت #${ro.variantId} · تعداد ${ro.qty}", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
            Spacer(Modifier.height(4.dp))
            Text(
                "هر ${ro.intervalDays} روز · " + (if (ro.isActive) "فعال" else "غیرفعال") + " · بعدی: ${ro.nextRunAt.take(10)}",
                color = colors.onSurfaceVariant, fontSize = FontSize.SMALL
            )
        }
        if (ro.isActive) {
            Text(
                "لغو",
                color = colors.sale,
                fontSize = FontSize.SMALL,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.sale.copy(alpha = 0.1f))
                    .clickable { onCancel() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
