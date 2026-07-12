package com.kazemieh.academy.placement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import org.koin.compose.viewmodel.koinViewModel

/** آزمونِ تعیینِ سطح — چند سؤالِ کوتاه، در پایان سطحِ پیشنهادی و لینک به دوره‌های همان سطح. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacementQuizScreen(
    navigateBack: () -> Unit,
    navigateToCoursesByLevel: (String) -> Unit
) {
    val viewModel = koinViewModel<PlacementQuizViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("آزمونِ تعیینِ سطح", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface, titleContentColor = colors.onSurface)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val result = state.result
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = colors.primary) }
                result != null -> Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(40.dp))
                    Text("سطحِ پیشنهادیِ تو:", color = colors.onSurfaceVariant, fontSize = FontSize.REGULAR)
                    Spacer(Modifier.height(10.dp))
                    Text(result.label, fontWeight = FontWeight.ExtraBold, color = colors.primary, fontSize = FontSize.EXTRA_LARGE)
                    Spacer(Modifier.height(28.dp))
                    Text(
                        "مشاهده‌ی دوره‌های همین سطح",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.button))
                            .background(colors.primary)
                            .clickable { navigateToCoursesByLevel(result.level) }
                            .padding(vertical = 14.dp),
                        textAlign = TextAlign.Center,
                        color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.REGULAR
                    )
                }
                else -> Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f).responsiveMaxWidth().padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        itemsIndexed(state.questions) { qIndex, question ->
                            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                                Text(question.text, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
                                Spacer(Modifier.height(10.dp))
                                question.options.forEachIndexed { oIndex, option ->
                                    val selected = state.answers[qIndex] == oIndex
                                    Text(
                                        option.label,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(Radius.sm))
                                            .background(if (selected) colors.accentSoft else colors.surface)
                                            .clickable { viewModel.selectAnswer(qIndex, oIndex) }
                                            .padding(12.dp),
                                        color = if (selected) colors.primary else colors.onSurface,
                                        fontSize = FontSize.SMALL,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                    val allAnswered = state.questions.isNotEmpty() && state.questions.indices.all { state.answers.containsKey(it) }
                    Text(
                        if (state.submitting) "در حالِ محاسبه…" else "مشاهده‌ی نتیجه",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(Radius.button))
                            .background(if (allAnswered && !state.submitting) colors.primary else colors.line)
                            .clickable(enabled = allAnswered && !state.submitting) { viewModel.submit() }
                            .padding(vertical = 14.dp),
                        textAlign = TextAlign.Center,
                        color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.REGULAR
                    )
                }
            }
        }
    }
}
