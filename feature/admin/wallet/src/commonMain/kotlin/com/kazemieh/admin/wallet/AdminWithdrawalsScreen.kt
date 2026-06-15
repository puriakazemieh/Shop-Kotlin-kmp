package com.kazemieh.admin.wallet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.designsystem.util.anyToString
import com.kazemieh.domain.wallet.AdminWithdrawal
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWithdrawalsScreen(
    onBackClick: () -> Unit
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
            TopAppBar(
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
                    val withdrawals = result.data
                    if (withdrawals.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(Resources.String.NothingHere), fontFamily = AppFont())
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
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
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Resources.String.UserIdLabelFormat, withdrawal.userId),
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFont()
                )
                Text(
                    text = withdrawal.status,
                    color = Color(0xFFFFB300),
                    fontWeight = FontWeight.Bold,
                    fontSize = FontSize.EXTRA_SMALL,
                    fontFamily = AppFont()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${stringResource(Resources.String.Iban)}: ${withdrawal.iban}",
                fontSize = FontSize.SMALL,
                fontFamily = AppFont()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = withdrawal.createdAt.take(16).replace("T", " "),
                    fontSize = FontSize.EXTRA_SMALL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = AppFont()
                )
                Text(
                    text = stringResource(Resources.String.PriceFormat, withdrawal.amount),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = AppFont()
                )
            }
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
                Text("مبلغ: ${withdrawal.amount} تومان", fontFamily = AppFont())
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
