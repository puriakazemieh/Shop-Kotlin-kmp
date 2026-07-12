package com.kazemieh.admin.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.kazemieh.domain.wallet.AdminWalletUser
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWalletScreen(
    onBackClick: () -> Unit,
    embedded: Boolean = false
) {
    val viewModel = koinViewModel<AdminWalletViewModel>()
    val state by viewModel.state.collectAsState()
    val messageBarState = rememberMessageBarState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    var selectedUser by remember { mutableStateOf<AdminWalletUser?>(null) }
    var showAdjustDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AdminWalletEffect.ShowError -> messageBarState.addError(effect.message)
                is AdminWalletEffect.AdjustSuccess -> {
                    messageBarState.addSuccess("تغییر موجودی با موفقیت انجام شد")
                    showAdjustDialog = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            if (!embedded) TopAppBar(
                title = { Text(stringResource(Resources.String.ManageWallets), fontFamily = AppFont()) },
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
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.handleIntent(AdminWalletIntent.Search(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Resources.String.WalletSearchUser), fontFamily = AppFont()) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                when (val result = state.usersState) {
                    is AppResult.Loading -> LoadingCard(Modifier.fillMaxSize())
                    is AppResult.Error -> InfoCard(
                        title = stringResource(Resources.String.Oops),
                        subtitle = anyToString(result.message),
                        image = Resources.Image.Cat
                    )
                    is AppResult.Success -> {
                        val users = result.data
                        if (users.isEmpty() && state.searchQuery.length >= 2) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(stringResource(Resources.String.NothingHere), fontFamily = AppFont())
                            }
                        } else {
                            LazyColumn(modifier = Modifier.responsiveMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(users) { user ->
                                    AdminUserWalletItem(
                                        user = user,
                                        onClick = {
                                            selectedUser = user
                                            showAdjustDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdjustDialog && selectedUser != null) {
        AdjustBalanceDialog(
            user = selectedUser!!,
            isLoading = state.isAdjusting,
            onDismiss = { showAdjustDialog = false },
            onConfirm = { amount, desc ->
                viewModel.handleIntent(AdminWalletIntent.AdjustBalance(selectedUser!!.userId, amount, desc))
            }
        )
    }
}

@Composable
fun AdminUserWalletItem(user: AdminWalletUser, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.fullName.take(1).uppercase(),
                fontFamily = AppFont(),
                fontWeight = FontWeight.ExtraBold,
                color = colors.primary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.fullName, fontFamily = AppFont(), fontWeight = FontWeight.Bold, color = colors.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(user.email, fontSize = FontSize.SMALL, color = colors.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stringResource(Resources.String.PriceFormat, user.balance),
            fontFamily = AppFont(),
            fontWeight = FontWeight.ExtraBold,
            color = colors.primary
        )
    }
}

@Composable
fun AdjustBalanceDialog(
    user: AdminWalletUser,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double, String?) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Resources.String.AdjustBalance), fontFamily = AppFont()) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${user.fullName} (${user.email})", fontFamily = AppFont())
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(Resources.String.Amount), fontFamily = AppFont()) },
                    placeholder = { Text(stringResource(Resources.String.PositiveForCharge), fontFamily = AppFont(), fontSize = FontSize.EXTRA_SMALL) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(Resources.String.Description), fontFamily = AppFont()) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { amount.toDoubleOrNull()?.let { onConfirm(it, description.ifEmpty { null }) } },
                enabled = !isLoading && amount.isNotEmpty()
            ) {
                Text(stringResource(Resources.String.Confirm), fontFamily = AppFont())
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Resources.String.Cancel), fontFamily = AppFont())
            }
        }
    )
}
