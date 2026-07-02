package com.kazemieh.academy.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircle
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
import com.kazemieh.details.VideoPlayer
import com.kazemieh.domain.academy.Lesson
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseLearnScreen(
    slug: String,
    navigateBack: () -> Unit
) {
    val viewModel = koinViewModel<CourseLearnViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors

    LaunchedEffect(slug) { viewModel.load(slug) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(state.course?.title ?: "یادگیری", fontSize = FontSize.MEDIUM, color = colors.onSurface, maxLines = 1) },
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val course = state.course
            when {
                state.isLoading && course == null -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.primary)
                course != null -> Column(modifier = Modifier.fillMaxSize()) {
                    // ---- ناحیه‌ی پخشِ ویدیو ----
                    val lesson = state.selectedLesson
                    Box(
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(androidx.compose.ui.graphics.Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        val url = lesson?.videoUrl
                        if (url != null) {
                            VideoPlayer(url = url, modifier = Modifier.fillMaxSize())
                        } else {
                            Text(
                                "این درس برای مشاهده نیازمندِ ثبت‌نام است",
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = FontSize.SMALL
                            )
                        }
                    }

                    // ---- درسِ فعلی + دکمه‌ی تکمیل ----
                    if (lesson != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lesson.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = colors.onSurface)
                            if (lesson.videoUrl != null) {
                                val done = lesson.completed
                                Text(
                                    text = if (done) "تکمیل‌شده ✓" else "تکمیل کردم",
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(Radius.button))
                                        .background(if (done) colors.surfaceVariant else colors.primary)
                                        .clickable(enabled = !done) { viewModel.markComplete(lesson.id) }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = if (done) colors.ok else colors.onPrimary,
                                    fontSize = FontSize.SMALL,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // ---- فهرستِ درس‌ها ----
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        course.sections.forEach { section ->
                            item {
                                Spacer(Modifier.height(6.dp))
                                Text(section.title, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
                            }
                            items(section.lessons.size) { idx ->
                                val l = section.lessons[idx]
                                LearnLessonRow(
                                    lesson = l,
                                    selected = l.id == state.selectedLessonId,
                                    onClick = { if (l.videoUrl != null) viewModel.selectLesson(l.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LearnLessonRow(lesson: Lesson, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.sm))
            .background(if (selected) colors.accentSoft else colors.surface)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (lesson.completed) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
            contentDescription = null,
            tint = if (lesson.completed) colors.ok else colors.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.size(10.dp))
        Text(lesson.title, modifier = Modifier.weight(1f), color = colors.onSurface, fontSize = FontSize.REGULAR)
    }
}
