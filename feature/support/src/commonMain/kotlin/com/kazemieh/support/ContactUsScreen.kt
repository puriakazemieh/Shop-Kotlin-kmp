package com.kazemieh.support

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.Resources
import com.kazemieh.domain.support.SenderRole
import com.kazemieh.domain.support.SupportMessage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(
    onBackClick: () -> Unit
) {
    val viewModel = koinViewModel<SupportViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Resources.String.ContactUs),
                        fontSize = FontSize.MEDIUM,
                        color = colors.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            modifier = Modifier.graphicsLayer { rotationY = if (isRtl) 180f else 0f },
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = stringResource(Resources.String.BackArrowDesc),
                            tint = colors.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface
                )
            )
        },
        bottomBar = {
            SupportInputBar(
                value = state.input,
                sending = state.isSending,
                onValueChange = { viewModel.handleIntent(SupportIntent.UpdateInput(it)) },
                onSend = { viewModel.handleIntent(SupportIntent.Send) }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.messages.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = colors.primary
                    )
                }
                state.messages.isEmpty() -> {
                    Text(
                        text = "سؤال یا مشکلی دارید؟ همین‌جا برای ما بنویسید؛ تیم پشتیبانی پاسخ می‌دهد.",
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        textAlign = TextAlign.Center,
                        color = colors.onSurfaceVariant,
                        fontSize = FontSize.REGULAR
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
                    ) {
                        items(state.messages) { message -> MessageBubble(message) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: SupportMessage) {
    val colors = AppTheme.colors
    val isCustomer = message.senderRole == SenderRole.CUSTOMER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCustomer) Arrangement.End else Arrangement.Start
    ) {
        Text(
            text = message.body,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(
                    color = if (isCustomer) colors.primary else colors.surfaceVariant,
                    shape = RoundedCornerShape(Radius.md)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            color = if (isCustomer) colors.onPrimary else colors.onSurface,
            fontSize = FontSize.REGULAR
        )
    }
}

@Composable
private fun SupportInputBar(
    value: String,
    sending: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("پیام خود را بنویسید…") },
            shape = RoundedCornerShape(Radius.lg),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.line
            )
        )
        Spacer(Modifier.size(8.dp))
        if (sending) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = colors.primary)
        } else {
            val enabled = value.isNotBlank()
            Text(
                text = "ارسال",
                modifier = Modifier
                    .background(
                        color = if (enabled) colors.primary else colors.surfaceVariant,
                        shape = RoundedCornerShape(Radius.button)
                    )
                    .clickable(enabled = enabled) { onSend() }
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                color = if (enabled) colors.onPrimary else colors.onSurfaceVariant,
                fontSize = FontSize.REGULAR
            )
        }
    }
}
