package com.kazemieh.psychtest.take

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
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.psychtest.TestQuestion
import com.kazemieh.domain.psychtest.UserPsychTest
import com.kazemieh.domain.psychtest.UserTestStatus
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeTestScreen(
    userTestId: Long,
    navigateBack: () -> Unit
) {
    val viewModel = koinViewModel<TakeTestViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()

    LaunchedEffect(userTestId) { viewModel.load(userTestId) }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TakeTestEffect.ShowError -> messageBarState.addError(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(state.test?.title ?: "انجامِ تست", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
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
            val test = state.test
            val result = state.result
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = colors.primary) }
                result != null -> TestResultView(result)
                test != null -> LazyColumn(
                    modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(test.questions.size) { idx ->
                        val q = test.questions[idx]
                        QuestionCard(
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
                            textAlign = TextAlign.Center, color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.REGULAR
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(number: Int, question: TestQuestion, selected: Int?, onSelect: (Int) -> Unit) {
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
        Spacer(Modifier.height(12.dp))
        question.options.forEachIndexed { i, opt ->
            val active = selected == i
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(Radius.sm))
                    .border(1.dp, if (active) colors.primary else colors.line, RoundedCornerShape(Radius.sm))
                    .background(if (active) colors.accentSoft else colors.surface)
                    .clickable { onSelect(i) }
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(20.dp).clip(CircleShape)
                        .border(2.dp, if (active) colors.primary else colors.line, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (active) Box(Modifier.size(10.dp).clip(CircleShape).background(colors.primary))
                }
                Spacer(Modifier.size(12.dp))
                Text(opt.text, color = colors.onSurface, fontSize = FontSize.REGULAR, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TestResultView(result: UserPsychTest) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val awaiting = result.status == UserTestStatus.AWAITING_INTERPRETATION
        Text(
            if (awaiting) "پاسخ‌های شما ثبت شد" else "نتیجه‌ی تست",
            fontWeight = FontWeight.ExtraBold, fontSize = FontSize.EXTRA_MEDIUM, color = colors.onSurface
        )
        Spacer(Modifier.height(16.dp))
        if (awaiting) {
            Text(
                "این تست توسطِ مشاور تفسیر می‌شود. نتیجه پس از بررسی در «تست‌های من» نمایش داده خواهد شد.",
                color = colors.onSurfaceVariant, fontSize = FontSize.REGULAR, textAlign = TextAlign.Center
            )
        } else {
            result.totalScore?.let {
                Text("امتیازِ شما: $it", color = colors.onSurfaceVariant, fontSize = FontSize.REGULAR)
                Spacer(Modifier.height(12.dp))
            }
            if (!result.interpretation.isNullOrBlank()) {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md))
                        .background(colors.surfaceVariant).padding(16.dp)
                ) {
                    Text(result.interpretation!!, color = colors.onSurface, fontSize = FontSize.REGULAR, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
