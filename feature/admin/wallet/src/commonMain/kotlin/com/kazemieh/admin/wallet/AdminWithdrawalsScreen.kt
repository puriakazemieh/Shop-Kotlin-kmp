package com.kazemieh.admin.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.designsystem.util.anyToString
import com.kazemieh.designsystem.util.formatToman
import com.kazemieh.domain.wallet.AdminWithdrawal
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWithdrawalsScreen(
    onBackClick: () -> Unit,
    embedded: Boolean = false
) {
    val viewModel = koinViewModel<AdminWithdrawalsViewModel>()
    val state by viewModel.state.collectAsState()
    val messageBarState = rememberMessageBarState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    var selectedWithdrawal by remember { mutableStateOf<AdminWithdrawal?>(null) }
    var showProcessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AdminWithdrawalsEffect.ShowError -> messageBarState.addError(effect.message)
                is AdminWithdrawalsEffect.ProcessSuccess -> {
                    messageBarState.addSuccess("درخواست با موفقیت پردازش شد")
                    showProcessDialog = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            if (!embedded) TopAppBar(
                title = { Text(stringResource(Resources.String.Withdraw), fontFamily = AppFont()) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            modifier = Modifier.graphicsLayer { rotationY = if (isRtl) 180f else 0f },
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->
        ContentWithMessageBar(
            modifier = Modifier.padding(padding),
            messageBarState = messageBarState
        ) {
            when (val result = state.withdrawalsState) {
                is AppResult.Loading -> LoadingCard(Modifier.fillMaxSize())
                is AppResult.Error -> InfoCard(
                    title = stringResource(Resources.String.Oops),
                    subtitle = anyToString(result.message),
                    image = Resources.Image.Cat
                )
                is AppResult.Success -> {
                    val colors = AppTheme.colors
                    val all = result.data
                    val pendingCount = all.count { it.status.uppercase() == "PENDING" }
                    var selectedWStatus by remember { mutableStateOf<String?>(null) }
                    val withdrawals = if (selectedWStatus == null) all
                        else all.filter { it.status.uppercase() == selectedWStatus }
                    val chips = listOf(
                        null to "همه",
                        "PENDING" to "در انتظار",
                        "PAID" to "پرداخت‌شده",
                        "REJECTED" to "ردشده"
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (pendingCount > 0) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(colors.gold.copy(alpha = 0.12f))
                                        .border(1.dp, colors.gold.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("⚠", color = colors.gold, fontSize = FontSize.MEDIUM)
                                    Text(
                                        text = "$pendingCount درخواست برداشت در انتظار بررسی شماست",
                                        fontSize = FontSize.SMALL,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.gold,
                                        fontFamily = AppFont()
                                    )
                                }
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                chips.forEach { (value, label) ->
                                    val sel = selectedWStatus == value
                                    Text(
                                        text = label,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(11.dp))
                                            .then(
                                                if (sel) Modifier.background(colors.primary)
                                                else Modifier.background(colors.surface).border(1.dp, colors.line, RoundedCornerShape(11.dp))
                                            )
                                            .clickable { selectedWStatus = value }
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        fontSize = FontSize.SMALL,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sel) colors.onPrimary else colors.onSurfaceVariant,
                                        fontFamily = AppFont()
                                    )
                                }
                            }
                        }
                        if (withdrawals.isEmpty()) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                    Text(stringResource(Resources.String.NothingHere), fontFamily = AppFont(), color = colors.onSurfaceVariant)
                                }
                            }
                        } else {
                            items(withdrawals) { withdrawal ->
                                WithdrawalItem(
                                    withdrawal = withdrawal,
                                    onClick = {
                                        selectedWithdrawal = withdrawal
                                        showProcessDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showProcessDialog && selectedWithdrawal != null) {
        ProcessWithdrawalDialog(
            withdrawal = selectedWithdrawal!!,
            isLoading = state.isProcessing,
            onDismiss = { showProcessDialog = false },
            onConfirm = { status, note ->
                viewModel.handleIntent(AdminWithdrawalsIntent.ProcessWithdrawal(selectedWithdrawal!!.id, status, note))
            }
        )
    }
}

@Composable
fun WithdrawalItem(withdrawal: AdminWithdrawal, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val statusUpper = withdrawal.status.uppercase()
    val statusColor = when (statusUpper) {
        "PAID" -> colors.ok
        "REJECTED" -> colors.sale
        else -> colors.gold
    }
    val statusLabel = when (statusUpper) {
        "PAID" -> "پرداخت شد"
        "REJECTED" -> "رد شد"
        else -> "در انتظار بررسی"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = withdrawal.userFullName ?: withdrawal.userEmail ?: stringResource(Resources.String.UserIdLabelFormat, withdrawal.userId),
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFont(),
                    color = colors.onSurface
                )
                if (withdrawal.userFullName != null && withdrawal.userEmail != null) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = withdrawal.userEmail!!,
                        fontSize = FontSize.EXTRA_SMALL,
                        color = colors.onSurfaceVariant,
                        fontFamily = AppFont()
                    )
                }
            }
            Text(
                text = statusLabel,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor)
                    .padding(horizontal = 11.dp, vertical = 4.dp),
                fontSize = FontSize.EXTRA_SMALL,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = AppFont()
            )
        }
        Spacer(modifier = Modifier.height(11.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = withdrawal.iban,
                modifier = Modifier.weight(1f),
                fontSize = FontSize.SMALL,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurfaceVariant,
                fontFamily = AppFont()
            )
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(11.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = withdrawal.createdAt.take(16).replace("T", " "),
                fontSize = FontSize.EXTRA_SMALL,
                color = colors.onSurfaceVariant,
                fontFamily = AppFont()
            )
            Text(
                text = stringResource(Resources.String.PriceFormat, formatToman(withdrawal.amount)),
                fontWeight = FontWeight.ExtraBold,
                fontSize = FontSize.EXTRA_REGULAR,
                color = colors.onSurface,
                fontFamily = AppFont()
            )
        }
        if (statusUpper == "PENDING") {
            Spacer(modifier = Modifier.height(13.dp))
            Text(
                text = "بررسی و پردازش درخواست",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.primary)
                    .clickable { onClick() }
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center,
                fontSize = FontSize.SMALL,
                fontWeight = FontWeight.Bold,
                color = colors.onPrimary,
                fontFamily = AppFont()
            )
        } else {
            val note = withdrawal.adminNote?.takeIf { it.isNotBlank() }
                ?: if (statusUpper == "PAID") "پرداخت شد" else "—"
            Spacer(modifier = Modifier.height(11.dp))
            Text(
                text = "یادداشت: $note",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                fontSize = FontSize.SMALL,
                color = colors.onSurfaceVariant,
                fontFamily = AppFont()
            )
        }
    }
}

@Composable
fun ProcessWithdrawalDialog(
    withdrawal: AdminWithdrawal,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var adminNote by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("پردازش درخواست برداشت", fontFamily = AppFont()) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("کاربر: ${withdrawal.userFullName ?: withdrawal.userId}", fontFamily = AppFont())
                Text("ایمیل: ${withdrawal.userEmail ?: "-"}", fontFamily = AppFont())
                Text("مبلغ: ${formatToman(withdrawal.amount)} تومان", fontFamily = AppFont())
                Text("شبا: ${withdrawal.iban}", fontFamily = AppFont())
                OutlinedTextField(
                    value = adminNote,
                    onValueChange = { adminNote = it },
                    label = { Text("یادداشت ادمین", fontFamily = AppFont()) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { onConfirm("REJECTED", adminNote.ifEmpty { null }) },
                    enabled = !isLoading,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("رد کردن", fontFamily = AppFont())
                }
                Button(
                    onClick = { onConfirm("PAID", adminNote.ifEmpty { null }) },
                    enabled = !isLoading
                ) {
                    Text("تایید و پرداخت", fontFamily = AppFont())
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Resources.String.Cancel), fontFamily = AppFont())
            }
        }
    )
}
