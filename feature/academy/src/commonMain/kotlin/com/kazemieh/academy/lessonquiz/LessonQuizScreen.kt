package com.kazemieh.academy.lessonquiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.academy.LessonQuizResult
import com.kazemieh.domain.academy.QuizQuestion
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

/** آزمونِ کوتاهِ یک درس (checkpoint) — قبولی گواهی صادر نمی‌کند، فقط سنجشِ فهمِ همان درس است. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonQuizScreen(
    lessonId: Long,
    navigateBack: () -> Unit
) {
    val viewModel = koinViewModel<LessonQuizViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()

    LaunchedEffect(lessonId) { viewModel.load(lessonId) }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LessonQuizEffect.ShowError -> messageBarState.addError(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(state.quiz?.title ?: "آزمونِ درس", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
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
        ContentWithMessageBar(modifier = Modifier.padding(padding), messageBarState = messageBarState) {
            val quiz = state.quiz
            val result = state.result
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
                result != null -> LessonQuizResultView(result = result, onRetry = { viewModel.load(lessonId) }, onBack = navigateBack)
                quiz != null -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            "برای قبولی در این آزمون باید حداقل ${quiz.passScore}٪ سؤالات را درست پاسخ دهید.",
                            fontSize = FontSize.SMALL, color = colors.onSurfaceVariant
                        )
                    }
                    items(quiz.questions.size) { idx ->
                        val q = quiz.questions[idx]
                        LessonQuestionCard(
                            number = idx + 1,
                            question = q,
                            selected = state.answers[q.index],
                            onSelect = { opt -> viewModel.selectAnswer(q.index, opt) }
                        )
                    }
                    item {
                        val enabled = viewModel.allAnswered() && !state.submitting
                        Text(
                            if (state.submitting) "در حالِ ارسال…" else "ثبتِ پاسخ‌ها",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.button))
                                .background(if (enabled) colors.primary else colors.line)
                                .clickable(enabled = enabled) { viewModel.submit() }
                                .padding(vertical = 14.dp),
                            textAlign = TextAlign.Center,
                            color = colors.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = FontSize.REGULAR
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonQuestionCard(number: Int, question: QuizQuestion, selected: Int?, onSelect: (Int) -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
            .padding(14.dp)
    ) {
        Text("$number. ${question.text}", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
        Spacer(Modifier.height(10.dp))
        question.options.forEachIndexed { i, opt ->
            val active = selected == i
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(if (active) colors.accentSoft else colors.surfaceVariant)
                    .clickable { onSelect(i) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(18.dp).clip(CircleShape)
                        .background(if (active) colors.primary else colors.surface)
                        .border(1.dp, if (active) colors.primary else colors.line, CircleShape)
                )
                Spacer(Modifier.size(10.dp))
                Text(opt.text, color = colors.onSurface, fontSize = FontSize.REGULAR)
            }
        }
    }
}

@Composable
private fun LessonQuizResultView(result: LessonQuizResult, onRetry: () -> Unit, onBack: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (result.passed) colors.ok else colors.onSurfaceVariant,
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            if (result.passed) "تبریک! قبول شدید" else "متأسفانه قبول نشدید",
            fontWeight = FontWeight.ExtraBold, fontSize = FontSize.EXTRA_MEDIUM, color = colors.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text("نمره‌ی شما: ${result.score}٪ (حدِ نصاب: ${result.passScore}٪)", color = colors.onSurfaceVariant, fontSize = FontSize.REGULAR)
        Spacer(Modifier.height(24.dp))
        Text(
            if (result.passed) "بازگشت به درس" else "تلاشِ دوباره",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.button))
                .background(colors.primary)
                .clickable { if (result.passed) onBack() else onRetry() }
                .padding(vertical = 14.dp),
            textAlign = TextAlign.Center, color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.REGULAR
        )
    }
}
