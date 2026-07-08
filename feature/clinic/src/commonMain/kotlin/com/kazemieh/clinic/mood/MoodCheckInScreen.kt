package com.kazemieh.clinic.mood

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.kazemieh.domain.clinic.MoodCheckIn
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

private val MOOD_EMOJIS = listOf("😞", "😕", "😐", "🙂", "😄")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodCheckInScreen(navigateBack: () -> Unit) {
    val viewModel = koinViewModel<MoodCheckInViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()
    var selectedScore by remember { mutableStateOf(3) }
    var note by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MoodCheckInEffect.Submitted -> {
                    messageBarState.addSuccess("خلق‌وخویِ امروز ثبت شد.")
                    note = ""
                }
                is MoodCheckInEffect.ShowError -> messageBarState.addError(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("ثبتِ روزانه‌ی خلق‌وخو", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Text("امروز چه حسی داشتی؟", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MOOD_EMOJIS.forEachIndexed { idx, emoji ->
                            val score = idx + 1
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(Radius.md))
                                    .background(if (selectedScore == score) colors.primary.copy(alpha = 0.15f) else colors.surface)
                                    .clickable { selectedScore = score },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = FontSize.LARGE)
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("یادداشتِ اختیاری", fontSize = FontSize.SMALL, color = colors.onSurfaceVariant) },
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
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (state.isSubmitting) "در حالِ ثبت…" else "ثبتِ خلق‌وخویِ امروز",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.button))
                            .background(if (state.isSubmitting) colors.line else colors.primary)
                            .clickable(enabled = !state.isSubmitting) { viewModel.submit(selectedScore, note.trim().ifBlank { null }) }
                            .padding(vertical = 12.dp),
                        color = colors.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = FontSize.REGULAR,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Text("روندِ اخیر", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
                    Spacer(Modifier.height(8.dp))
                }
                if (state.history.isEmpty() && !state.isLoading) {
                    item {
                        Text("هنوز خلق‌وخویی ثبت نکرده‌اید.", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                    }
                } else {
                    items(state.history) { entry -> MoodHistoryRow(entry) }
                }
            }
        }
    }
}

@Composable
private fun MoodHistoryRow(entry: MoodCheckIn) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.sm))
            .background(colors.surface)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(MOOD_EMOJIS.getOrElse(entry.moodScore - 1) { "🙂" }, fontSize = FontSize.LARGE)
        Spacer(Modifier.height(0.dp))
        Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(entry.moodScore / 5f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.primary)
            )
            if (!entry.note.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(entry.note, color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
            }
        }
        Text(entry.createdAt?.take(10).orEmpty(), color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
    }
}
