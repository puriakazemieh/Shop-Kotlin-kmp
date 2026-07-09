package com.kazemieh.clinic.journal

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.kazemieh.domain.clinic.JournalEntry
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(navigateBack: () -> Unit) {
    val viewModel = koinViewModel<JournalViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()
    var draft by remember { mutableStateOf("") }
    var shareEnabled by remember { mutableStateOf(false) }
    var shareTherapistId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is JournalEffect.ShowError -> messageBarState.addError(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("یادداشتِ روزانه", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
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
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md)).background(colors.surface).padding(14.dp)
                    ) {
                        OutlinedTextField(
                            value = draft,
                            onValueChange = { draft = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("امروز چه گذشت؟", fontSize = FontSize.SMALL, color = colors.onSurfaceVariant) },
                            minLines = 3,
                            shape = RoundedCornerShape(Radius.sm),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.background,
                                unfocusedContainerColor = colors.background,
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.line,
                                cursorColor = colors.primary,
                                focusedTextColor = colors.onSurface,
                                unfocusedTextColor = colors.onSurface
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { shareEnabled = !shareEnabled }) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (shareEnabled) colors.primary else colors.surfaceVariant)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("اشتراک‌گذاری با درمانگر", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                        }
                        if (shareEnabled) {
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = shareTherapistId,
                                onValueChange = { shareTherapistId = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("شناسه‌ی درمانگر", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant) },
                                singleLine = true,
                                shape = RoundedCornerShape(Radius.sm),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = colors.background,
                                    unfocusedContainerColor = colors.background,
                                    focusedBorderColor = colors.primary,
                                    unfocusedBorderColor = colors.line,
                                    cursorColor = colors.primary,
                                    focusedTextColor = colors.onSurface,
                                    unfocusedTextColor = colors.onSurface
                                )
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (state.isSaving) "در حالِ ذخیره…" else "ثبتِ یادداشت",
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.button))
                                .background(if (draft.isNotBlank() && !state.isSaving) colors.primary else colors.line)
                                .clickable(enabled = draft.isNotBlank() && !state.isSaving) {
                                    viewModel.add(draft, if (shareEnabled) shareTherapistId.toLongOrNull() else null)
                                    draft = ""; shareEnabled = false; shareTherapistId = ""
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL
                        )
                    }
                }
                if (state.isLoading && state.entries.isEmpty()) {
                    item { CircularProgressIndicator(modifier = Modifier.fillMaxWidth().padding(24.dp), color = colors.primary) }
                } else if (state.entries.isEmpty()) {
                    item {
                        Text("هنوز یادداشتی ثبت نکرده‌اید.", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL, modifier = Modifier.padding(24.dp))
                    }
                } else {
                    items(state.entries) { entry -> JournalRow(entry, onDelete = { viewModel.delete(entry.id) }) }
                }
            }
        }
    }
}

@Composable
private fun JournalRow(entry: JournalEntry, onDelete: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md)).background(colors.surface).padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(entry.createdAt?.take(16)?.replace("T", " ").orEmpty(), color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL, modifier = Modifier.weight(1f))
            if (entry.sharedWithTherapistId != null) {
                Text("به‌اشتراک‌گذاشته‌شده", color = colors.primary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
            }
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(16.dp).clickable { onDelete() },
                tint = colors.sale
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(entry.content, color = colors.onSurface, fontSize = FontSize.SMALL)
    }
}
