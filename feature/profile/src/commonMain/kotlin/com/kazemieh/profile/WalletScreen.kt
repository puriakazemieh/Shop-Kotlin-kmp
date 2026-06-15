package com.kazemieh.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.component.PrimaryButton
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.designsystem.util.anyToString
import com.kazemieh.domain.wallet.WalletTransaction
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onBackClick: () -> Unit
) {
    val viewModel = koinViewModel<WalletViewModel>()
    val state by viewModel.state.collectAsState()
    val messageBarState = rememberMessageBarState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val uriHandler = LocalUriHandler.current

    var showTopUpDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WalletEffect.ShowError -> messageBarState.addError(effect.message)
                is WalletEffect.ShowSuccess -> messageBarState.addSuccess(effect.message)
                is WalletEffect.NavigateToPayment -> {
                    uriHandler.openUri(effect.url)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Resources.String.MyWallet), fontFamily = AppFont()) },
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
        },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = { showWithdrawDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(painterResource(Resources.Icon.Book), contentDescription = null, modifier = Modifier.size(20.dp))
                }
                FloatingActionButton(onClick = { showTopUpDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }
    ) { padding ->
        ContentWithMessageBar(
            modifier = Modifier.padding(padding),
            messageBarState = messageBarState
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                BalanceCard(state.balanceState)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = stringResource(Resources.String.Transactions),
                    fontFamily = AppFont(),
                    fontSize = FontSize.LARGE,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(modifier = Modifier.weight(1f)) {
                    TransactionsList(state.transactionsState)
                }
            }
        }
    }

    if (showTopUpDialog) {
        TopUpDialog(
            isLoading = state.isTopUpLoading,
            onDismiss = { showTopUpDialog = false },
            onConfirm = { amount ->
                viewModel.handleIntent(WalletIntent.TopUp(amount))
                showTopUpDialog = false
            }
        )
    }

    if (showWithdrawDialog) {
        WithdrawDialog(
            isLoading = state.isWithdrawLoading,
            onDismiss = { showWithdrawDialog = false },
            onConfirm = { amount, iban ->
                viewModel.handleIntent(WalletIntent.Withdraw(amount, iban))
                showWithdrawDialog = false
            }
        )
    }
}

@Composable
fun WithdrawDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var iban by remember { mutableStateOf("") }
    val isAmountValid = (amount.toDoubleOrNull() ?: 0.0) > 0
    val isIbanValid = iban.startsWith("IR") && iban.length == 26

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Resources.String.Withdraw), fontFamily = AppFont()) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(Resources.String.Amount), fontFamily = AppFont()) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = amount.isNotEmpty() && !isAmountValid
                )
                OutlinedTextField(
                    value = iban,
                    onValueChange = { iban = it },
                    label = { Text(stringResource(Resources.String.Iban), fontFamily = AppFont()) },
                    placeholder = { Text("IR...", fontFamily = AppFont()) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = iban.isNotEmpty() && !isIbanValid,
                    supportingText = {
                        if (iban.isNotEmpty() && !isIbanValid) {
                            Text(text = "فرمت شبا نامعتبر است (۲۶ رقم با IR شروع شود)")
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { amount.toDoubleOrNull()?.let { onConfirm(it, iban) } },
                enabled = !isLoading && isAmountValid && isIbanValid
            ) {
                if (isLoading) CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                else Text(stringResource(Resources.String.Confirm), fontFamily = AppFont())
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Resources.String.Cancel), fontFamily = AppFont())
            }
        }
    )
}

@Composable
fun BalanceCard(balanceState: AppResult<com.kazemieh.domain.wallet.WalletBalance>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(Resources.String.WalletBalance), fontFamily = AppFont())
            Spacer(modifier = Modifier.height(8.dp))
            when (balanceState) {
                is AppResult.Loading -> CircularProgressIndicator()
                is AppResult.Success -> {
                    Text(
                        text = stringResource(Resources.String.PriceFormat, balanceState.data.balance),
                        fontFamily = AppFont(),
                        fontSize = FontSize.EXTRA_LARGE,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is AppResult.Error -> Text(anyToString(balanceState.message), color = MaterialTheme.colorScheme.error, fontFamily = AppFont())
            }
        }
    }
}

@Composable
fun TransactionsList(state: AppResult<com.kazemieh.domain.admin.AdminPage<WalletTransaction>>) {
    when (state) {
        is AppResult.Loading -> LoadingCard(Modifier.fillMaxWidth().height(200.dp))
        is AppResult.Error -> InfoCard(
            title = stringResource(Resources.String.Oops),
            subtitle = anyToString(state.message),
            image = Resources.Image.Cat
        )
        is AppResult.Success -> {
            val transactions = state.data.items
            if (transactions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(Resources.String.WalletEmpty), fontFamily = AppFont())
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { transaction ->
                        TransactionItem(transaction)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: WalletTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description ?: transaction.type,
                    fontFamily = AppFont(),
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
                Text(
                    text = transaction.createdAt.take(16).replace("T", " "),
                    fontSize = FontSize.SMALL,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(Resources.String.PriceFormat, transaction.amount),
                fontFamily = AppFont(),
                fontWeight = FontWeight.Bold,
                color = if (transaction.amount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun TopUpDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    val isValid = (amount.toDoubleOrNull() ?: 0.0) > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Resources.String.TopUp), fontFamily = AppFont()) },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(Resources.String.Amount), fontFamily = AppFont()) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = amount.isNotEmpty() && !isValid
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { amount.toDoubleOrNull()?.let { onConfirm(it) } },
                enabled = !isLoading && isValid
            ) {
                if (isLoading) CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                else Text(stringResource(Resources.String.Confirm), fontFamily = AppFont())
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Resources.String.Cancel), fontFamily = AppFont())
            }
        }
    )
}
