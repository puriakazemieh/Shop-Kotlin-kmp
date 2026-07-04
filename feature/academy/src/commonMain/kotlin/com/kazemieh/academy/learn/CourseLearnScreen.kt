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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.details.VideoPlayer
import com.kazemieh.domain.academy.Lesson
import org.koin.compose.viewmodel.koinViewModel

private enum class LessonTab { VIDEO, FILES, QUIZ }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseLearnScreen(
    slug: String,
    navigateBack: () -> Unit,
    navigateToQuiz: (Long) -> Unit = {},
    navigateToLessonQuiz: (Long) -> Unit = {},
    navigateToProject: (Long) -> Unit = {}
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
                    // کیفیتِ انتخاب‌شده per درس (ریست با تغییرِ درس)
                    var selectedQuality by remember(lesson?.id) { mutableStateOf<String?>(null) }
                    val activeUrl = remember(lesson?.id, selectedQuality) {
                        val variants = lesson?.videoVariants.orEmpty()
                        when {
                            selectedQuality != null -> variants.firstOrNull { it.quality == selectedQuality }?.url ?: lesson?.videoUrl
                            else -> lesson?.videoUrl ?: variants.firstOrNull()?.url
                        }
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(androidx.compose.ui.graphics.Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        if (activeUrl != null) {
                            VideoPlayer(url = activeUrl, modifier = Modifier.fillMaxSize())
                        } else {
                            Text(
                                "این درس برای مشاهده نیازمندِ ثبت‌نام است",
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = FontSize.SMALL
                            )
                        }
                    }

                    // ---- انتخابِ کیفیتِ پخش (اگر چند کیفیت موجود باشد) ----
                    val variants = lesson?.videoVariants.orEmpty()
                    if (variants.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("کیفیت:", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                            variants.forEach { v ->
                                val active = (selectedQuality ?: variants.firstOrNull()?.quality) == v.quality
                                Text(
                                    v.quality,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(Radius.full))
                                        .background(if (active) colors.primary else colors.surfaceVariant)
                                        .clickable { selectedQuality = v.quality }
                                        .padding(horizontal = 12.dp, vertical = 5.dp),
                                    color = if (active) colors.onPrimary else colors.onSurfaceVariant,
                                    fontSize = FontSize.EXTRA_SMALL,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // ---- دکمه‌ی آزمونِ پایانِ دوره (پس از تکمیلِ ۱۰۰٪) ----
                    if (course.progressPercent >= 100) {
                        Text(
                            "شرکت در آزمونِ پایانِ دوره",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(Radius.button))
                                .background(colors.gold)
                                .clickable { navigateToQuiz(course.id) }
                                .padding(vertical = 12.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = FontSize.SMALL
                        )
                    }

                    // ---- تبِ ویدیو/فایل‌ها/آزمون (فقط اگر درسِ فعلی فایل یا آزمون داشته باشد) ----
                    var activeTab by remember(lesson?.id) { mutableStateOf(LessonTab.VIDEO) }
                    if (lesson != null && (lesson.resourceFiles.isNotEmpty() || lesson.hasQuiz)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LessonTabChip("ویدیو", activeTab == LessonTab.VIDEO) { activeTab = LessonTab.VIDEO }
                            if (lesson.resourceFiles.isNotEmpty()) {
                                LessonTabChip("فایل‌ها", activeTab == LessonTab.FILES) { activeTab = LessonTab.FILES }
                            }
                            if (lesson.hasQuiz) {
                                LessonTabChip("آزمون", activeTab == LessonTab.QUIZ) { activeTab = LessonTab.QUIZ }
                            }
                        }
                        when (activeTab) {
                            LessonTab.FILES -> {
                                val uriHandler = LocalUriHandler.current
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                                    lesson.resourceFiles.forEach { file ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(Radius.sm))
                                                .background(colors.surfaceVariant)
                                                .clickable { uriHandler.openUri(file.url) }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(file.name, modifier = Modifier.weight(1f), color = colors.onSurface, fontSize = FontSize.SMALL)
                                            if (file.sizeLabel != null) {
                                                Text(file.sizeLabel, color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
                                            }
                                        }
                                        Spacer(Modifier.height(6.dp))
                                    }
                                }
                            }
                            LessonTab.QUIZ -> {
                                Text(
                                    "شرکت در آزمونِ این درس",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                        .clip(RoundedCornerShape(Radius.button))
                                        .background(colors.primary)
                                        .clickable { navigateToLessonQuiz(lesson.id) }
                                        .padding(vertical = 12.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL
                                )
                            }
                            LessonTab.VIDEO -> {}
                        }
                    }

                    // ---- پروژه‌ی پایانی (فقط دوره‌های پروژه‌محور، پس از تکمیلِ ۱۰۰٪) ----
                    if (course.requiresProjectSubmission && course.progressPercent >= 100) {
                        Text(
                            "ثبتِ پروژه‌ی پایانی",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(Radius.button))
                                .background(colors.accent2)
                                .clickable { navigateToProject(course.id) }
                                .padding(vertical = 12.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL
                        )
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
private fun LessonTabChip(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        label,
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.full))
            .background(if (active) colors.primary else colors.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        color = if (active) colors.onPrimary else colors.onSurfaceVariant,
        fontSize = FontSize.SMALL,
        fontWeight = FontWeight.SemiBold
    )
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
