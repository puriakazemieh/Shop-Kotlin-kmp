package com.kazemieh.academy.detail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
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
import com.kazemieh.domain.academy.CourseDetail
import com.kazemieh.domain.academy.Lesson
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    slug: String,
    navigateBack: () -> Unit,
    navigateToLearn: (String) -> Unit
) {
    val viewModel = koinViewModel<CourseDetailViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors

    LaunchedEffect(slug) { viewModel.load(slug) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("جزئیات دوره", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
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
        },
        bottomBar = {
            val course = state.course
            if (course != null) {
                CourseBottomAction(
                    course = course,
                    enrolling = state.isEnrolling,
                    onEnroll = { viewModel.enroll() },
                    onContinue = { navigateToLearn(course.slug) }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val course = state.course
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.primary)
                course != null -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
                ) {
                    item {
                        Text(course.title, fontWeight = FontWeight.ExtraBold, fontSize = FontSize.EXTRA_REGULAR, color = colors.onSurface)
                        if (!course.instructor.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text("مدرس: ${course.instructor}", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                        }
                        if (course.enrolled && course.progressPercent > 0) {
                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { course.progressPercent / 100f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(Radius.full)),
                                color = colors.primary,
                                trackColor = colors.surfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("${course.progressPercent}% تکمیل", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                        }
                    }
                    if (!course.description.isNullOrBlank()) {
                        item {
                            Text(course.description!!, color = colors.onSurfaceVariant, fontSize = FontSize.REGULAR)
                        }
                    }
                    course.sections.forEach { section ->
                        item {
                            Spacer(Modifier.height(6.dp))
                            Text(section.title, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
                        }
                        items(section.lessons.size) { idx ->
                            LessonRow(section.lessons[idx], course.enrolled)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonRow(lesson: Lesson, enrolled: Boolean) {
    val colors = AppTheme.colors
    val accessible = enrolled || lesson.isFreePreview
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.sm))
            .background(colors.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = when {
            lesson.completed -> Icons.Default.CheckCircle
            accessible -> Icons.Default.PlayCircle
            else -> Icons.Default.Lock
        }
        val tint = when {
            lesson.completed -> colors.ok
            accessible -> colors.primary
            else -> colors.onSurfaceVariant
        }
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(10.dp))
        Text(lesson.title, modifier = Modifier.weight(1f), color = colors.onSurface, fontSize = FontSize.REGULAR)
        if (lesson.isFreePreview && !enrolled) {
            Text("پیش‌نمایش", color = colors.ok, fontSize = FontSize.SMALL)
        }
    }
}

@Composable
private fun CourseBottomAction(
    course: CourseDetail,
    enrolling: Boolean,
    onEnroll: () -> Unit,
    onContinue: () -> Unit
) {
    val colors = AppTheme.colors
    val label = when {
        enrolling -> "در حال ثبت‌نام…"
        course.enrolled -> "ادامه‌ی یادگیری"
        else -> "ثبت‌نام در دوره"
    }
    Box(modifier = Modifier.fillMaxWidth().background(colors.surface).padding(16.dp)) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.button))
                .background(colors.primary)
                .clickable(enabled = !enrolling) { if (course.enrolled) onContinue() else onEnroll() }
                .padding(vertical = 15.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = colors.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = FontSize.REGULAR
        )
    }
}
