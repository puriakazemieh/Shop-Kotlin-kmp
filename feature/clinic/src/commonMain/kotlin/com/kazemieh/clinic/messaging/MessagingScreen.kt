package com.kazemieh.clinic.messaging

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.clinic.ClinicMessage
import com.kazemieh.domain.clinic.MessageSenderType
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingScreen(
    therapistId: Long,
    navigateBack: () -> Unit
) {
    val viewModel = koinViewModel<MessagingViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(therapistId) { viewModel.load(therapistId) }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MessagingEffect.ShowError -> messageBarState.addError(effect.message)
            }
        }
    }
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.size - 1)
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("گفتگو با درمانگر", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.onSurface,
                    navigationIconContentColor = colors.onSurface
                )
            )
        }
    ) { padding ->
        ContentWithMessageBar(
            modifier = Modifier.fillMaxSize().padding(padding),
            messageBarState = messageBarState
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                state.status?.let { status ->
                    Text(
                        text = if (status.active) "اشتراکِ پیام‌رسانیِ نامحدود فعال است"
                        else "پیام‌هایِ رایگانِ باقی‌مانده: ${status.freeMessagesRemaining}",
                        color = if (status.active) colors.ok else colors.onSurfaceVariant,
                        fontSize = FontSize.EXTRA_SMALL,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        state.isLoading && state.messages.isEmpty() ->
                            CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.primary)
                        state.messages.isEmpty() -> Text(
                            "هنوز پیامی رد و بدل نشده. اولین پیام را بفرست.",
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            color = colors.onSurfaceVariant
                        )
                        else -> LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            items(state.messages) { message -> MessageBubble(message) }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("پیامت را بنویس…", fontSize = FontSize.SMALL, color = colors.onSurfaceVariant) },
                        shape = RoundedCornerShape(Radius.md),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.line,
                            cursorColor = colors.primary,
                            focusedTextColor = colors.onSurface,
                            unfocusedTextColor = colors.onSurface
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        enabled = draft.isNotBlank() && !state.isSending,
                        onClick = { viewModel.send(draft); draft = "" }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "ارسال",
                            tint = if (draft.isNotBlank() && !state.isSending) colors.primary else colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ClinicMessage) {
    val colors = AppTheme.colors
    val isPatient = message.senderType == MessageSenderType.PATIENT
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isPatient) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(Radius.md))
                .background(if (isPatient) colors.primary else colors.surface)
                .padding(10.dp)
        ) {
            Text(
                message.body,
                color = if (isPatient) colors.onPrimary else colors.onSurface,
                fontSize = FontSize.SMALL
            )
            if (!message.createdAt.isNullOrBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    message.createdAt.take(16).replace("T", " "),
                    color = if (isPatient) colors.onPrimary.copy(alpha = 0.7f) else colors.onSurfaceVariant,
                    fontSize = FontSize.EXTRA_SMALL,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
