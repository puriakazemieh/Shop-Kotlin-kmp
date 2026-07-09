package com.kazemieh.clinic.match

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.clinic.TherapistMatchResult
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapistMatchScreen(
    navigateBack: () -> Unit,
    navigateToTherapist: (String) -> Unit
) {
    val viewModel = koinViewModel<TherapistMatchViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TherapistMatchEffect.ShowError -> messageBarState.addError(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("پرسشنامه‌ی تطبیقِ درمانگر", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
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
            val results = state.results
            when {
                state.isLoading && state.questions.isEmpty() ->
                    Box(Modifier.fillMaxSize()) { CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.primary) }
                results != null -> ResultsList(results, navigateToTherapist)
                else -> QuestionsList(state.questions, state.selectedTags, viewModel::toggleTag, state.isSubmitting, viewModel::submit)
            }
        }
    }
}

@Composable
private fun QuestionsList(
    questions: List<com.kazemieh.domain.clinic.TherapistMatchQuestion>,
    selectedTags: Set<String>,
    onToggle: (String) -> Unit,
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    val colors = AppTheme.colors
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text("مواردی که با شما هم‌خوانی دارند را انتخاب کنید:", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
            Spacer(Modifier.height(4.dp))
        }
        if (questions.isEmpty()) {
            item { Text("هنوز سؤالی تعریف نشده.", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL) }
        } else {
            items(questions) { q ->
                val selected = q.tag in selectedTags
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.md))
                        .background(if (selected) colors.primary.copy(alpha = 0.12f) else colors.surface)
                        .clickable { onToggle(q.tag) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(q.questionText, color = colors.onSurface, fontSize = FontSize.SMALL, modifier = Modifier.weight(1f))
                    if (selected) {
                        Text("✓", color = colors.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item {
                Spacer(Modifier.height(6.dp))
                Text(
                    if (isSubmitting) "در حالِ یافتنِ بهترین گزینه‌ها…" else "یافتنِ درمانگرِ مناسب",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.button))
                        .background(if (selectedTags.isNotEmpty() && !isSubmitting) colors.primary else colors.line)
                        .clickable(enabled = selectedTags.isNotEmpty() && !isSubmitting) { onSubmit() }
                        .padding(vertical = 12.dp),
                    color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.REGULAR,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ResultsList(results: List<TherapistMatchResult>, navigateToTherapist: (String) -> Unit) {
    val colors = AppTheme.colors
    if (results.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("موردی که با این معیارها هم‌خوانی داشته باشد یافت نشد.", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL, modifier = Modifier.padding(24.dp))
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item { Text("درمانگرهایِ پیشنهادی", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR) }
        items(results) { r ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.md))
                    .background(colors.surface)
                    .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
                    .clickable { navigateToTherapist(r.therapist.slug) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(r.therapist.name, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
                    if (!r.therapist.specialty.isNullOrBlank()) {
                        Spacer(Modifier.height(3.dp))
                        Text(r.therapist.specialty, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                    }
                }
                Text("امتیازِ تطبیق: ${r.matchScore}", color = colors.primary, fontWeight = FontWeight.SemiBold, fontSize = FontSize.SMALL)
            }
        }
    }
}
