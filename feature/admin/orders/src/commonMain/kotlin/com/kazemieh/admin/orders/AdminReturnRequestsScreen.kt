package com.kazemieh.admin.orders

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.order.AdminReturnRequest
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReturnRequestsScreen(onBackClick: () -> Unit, embedded: Boolean = false) {
    val viewModel = koinViewModel<AdminReturnRequestsViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AdminReturnRequestsEffect.ShowError -> messageBarState.addError(effect.message)
                is AdminReturnRequestsEffect.ShowSuccess -> messageBarState.addSuccess(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = colors.surface,
        topBar = {
            if (!embedded) TopAppBar(
                title = { Text("درخواست‌هایِ مرجوعی/تعویض", color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface, titleContentColor = colors.onSurface)
            )
        }
    ) { padding ->
        ContentWithMessageBar(modifier = Modifier.padding(padding), messageBarState = messageBarState) {
            if (state.isLoading && state.requests.isEmpty()) {
                LoadingCard(modifier = Modifier.fillMaxSize())
            } else if (state.requests.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("درخواستی ثبت نشده.", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.requests) { req ->
                        AdminReturnRequestRow(
                            req = req,
                            onApprove = { viewModel.updateStatus(req.id, "APPROVED", null) },
                            onReject = { viewModel.updateStatus(req.id, "REJECTED", null) },
                            onComplete = { viewModel.updateStatus(req.id, "COMPLETED", null) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminReturnRequestRow(
    req: AdminReturnRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text("${req.itemTitle} — سفارش #${req.orderId}", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
        Spacer(Modifier.height(4.dp))
        Text(
            "${req.userName ?: "کاربر #${req.userId}"} · ${if (req.type == "EXCHANGE") "تعویض" else "مرجوعی"} · ${req.status}",
            color = colors.onSurfaceVariant, fontSize = FontSize.SMALL
        )
        Spacer(Modifier.height(6.dp))
        Text(req.reason, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)

        if (req.status == "PENDING") {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionChip("تایید", colors.ok, onApprove)
                ActionChip("رد", colors.sale, onReject)
            }
        } else if (req.status == "APPROVED") {
            Spacer(Modifier.height(10.dp))
            ActionChip("علامت‌گذاریِ تکمیل‌شده", colors.primary, onComplete)
        }
    }
}

@Composable
private fun ActionChip(label: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Text(
        label,
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(color.copy(alpha = 0.12f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        color = color,
        fontSize = FontSize.EXTRA_SMALL,
        fontWeight = FontWeight.SemiBold
    )
}
