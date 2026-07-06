package com.kazemieh.orders.returns

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.component.PrimaryButton
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.order.ReturnRequest
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

private val statusLabels = mapOf(
    "PENDING" to "در انتظارِ بررسی", "APPROVED" to "تاییدشده",
    "REJECTED" to "ردشده", "COMPLETED" to "تکمیل‌شده"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnRequestScreen(
    orderItemId: Long?,
    itemTitle: String?,
    onBackClick: () -> Unit
) {
    val viewModel = koinViewModel<ReturnRequestViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()

    var type by remember { mutableStateOf("RETURN") }
    var reason by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.handleIntent(ReturnRequestIntent.LoadMine) }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ReturnRequestEffect.ShowError -> messageBarState.addError(effect.message)
                ReturnRequestEffect.Submitted -> {
                    messageBarState.addSuccess("درخواست ثبت شد.")
                    reason = ""
                }
            }
        }
    }

    Scaffold(
        containerColor = colors.surface,
        topBar = {
            TopAppBar(
                title = { Text("مرجوعی و تعویض", color = colors.onSurface) },
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
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (orderItemId != null) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.surfaceVariant)
                                .padding(14.dp)
                        ) {
                            Text("درخواستِ جدید برایِ: ${itemTitle ?: ""}", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TypeChip("مرجوعی", selected = type == "RETURN", onClick = { type = "RETURN" })
                                TypeChip("تعویض", selected = type == "EXCHANGE", onClick = { type = "EXCHANGE" })
                            }
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = reason,
                                onValueChange = { reason = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("دلیلِ درخواست", fontSize = FontSize.EXTRA_SMALL) },
                                minLines = 2
                            )
                            Spacer(Modifier.height(10.dp))
                            PrimaryButton(
                                text = "ثبتِ درخواست",
                                enabled = reason.isNotBlank() && !state.isSubmitting,
                                onClick = { viewModel.handleIntent(ReturnRequestIntent.Submit(orderItemId, type, reason)) }
                            )
                        }
                    }
                }

                item {
                    Text("درخواست‌هایِ من", fontWeight = FontWeight.ExtraBold, color = colors.onSurface, fontSize = FontSize.MEDIUM)
                }

                when (val result = state.myRequests) {
                    is AppResult.Loading -> item { LoadingCard(modifier = Modifier.fillMaxWidth()) }
                    is AppResult.Error -> item { Text(result.message.toString(), color = colors.sale) }
                    is AppResult.Success -> {
                        if (result.data.isEmpty()) {
                            item { Text("هنوز درخواستی ثبت نشده.", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL) }
                        } else {
                            items(result.data) { req -> ReturnRequestRow(req) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        label,
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) colors.primary else colors.surface)
            .border(1.dp, if (selected) colors.primary else colors.line, RoundedCornerShape(9.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        color = if (selected) colors.onPrimary else colors.onSurface,
        fontSize = FontSize.SMALL,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun ReturnRequestRow(req: ReturnRequest) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(req.itemTitle, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
        Spacer(Modifier.height(4.dp))
        Text(
            (if (req.type == "EXCHANGE") "تعویض" else "مرجوعی") + " · " + (statusLabels[req.status] ?: req.status),
            color = colors.onSurfaceVariant, fontSize = FontSize.SMALL
        )
        Spacer(Modifier.height(4.dp))
        Text(req.reason, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
        req.adminNote?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(4.dp))
            Text("پاسخِ ادمین: $it", color = colors.primary, fontSize = FontSize.SMALL)
        }
    }
}
