package com.kazemieh.clinic.homework

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.clinic.Homework
import com.kazemieh.domain.clinic.HomeworkStatus
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkScreen(navigateBack: () -> Unit) {
    val viewModel = koinViewModel<HomeworkViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is HomeworkEffect.ShowError -> messageBarState.addError(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("تکلیف‌ها و تمرین‌ها", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
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
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading && state.homework.isEmpty() ->
                        CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.primary)
                    state.homework.isEmpty() -> Text(
                        "هنوز تکلیفی برایت تعیین نشده.",
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        color = colors.onSurfaceVariant
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(state.homework) { hw ->
                            HomeworkCard(
                                homework = hw,
                                completing = state.completingId == hw.id,
                                onComplete = { viewModel.complete(hw.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeworkCard(
    homework: Homework,
    completing: Boolean,
    onComplete: () -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(homework.title, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR, modifier = Modifier.weight(1f))
            Text(
                if (homework.status == HomeworkStatus.COMPLETED) "انجام‌شده" else "بازِ انجام",
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.full))
                    .background((if (homework.status == HomeworkStatus.COMPLETED) colors.ok else colors.star).copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                color = if (homework.status == HomeworkStatus.COMPLETED) colors.ok else colors.star,
                fontSize = FontSize.EXTRA_SMALL,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(4.dp))
        Text("از طرفِ: ${homework.therapistName}", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
        if (!homework.description.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(homework.description, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
        }
        if (!homework.dueDate.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text("مهلت: ${homework.dueDate.take(10)}", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
        }
        if (homework.status == HomeworkStatus.ASSIGNED) {
            Spacer(Modifier.height(10.dp))
            Text(
                if (completing) "در حالِ ثبت…" else "علامت‌گذاریِ به‌عنوانِ انجام‌شده",
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.button))
                    .background(if (completing) colors.line else colors.primary)
                    .clickable(enabled = !completing) { onComplete() }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                color = colors.onPrimary,
                fontSize = FontSize.SMALL,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
