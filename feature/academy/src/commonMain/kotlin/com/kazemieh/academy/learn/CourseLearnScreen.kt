package com.kazemieh.academy.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.details.VideoPlayer
import com.kazemieh.domain.academy.Lesson
import org.koin.compose.viewmodel.koinViewModel

private enum class LessonTab { VIDEO, FILES, QUIZ, QA }

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
                course != null -> Column(modifier = Modifier.fillMaxSize().responsiveMaxWidth()) {
                    // ---- نوارِ پیشرفتِ دوره ----
                    if (course.isOnline) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { (course.progressPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(Radius.full)),
                                color = colors.primary,
                                trackColor = colors.surfaceVariant
                            )
                            Text("${course.progressPercent}%", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL, fontWeight = FontWeight.SemiBold)
                        }
                    }
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
                            Text("کیفیتِ پخش:", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                            variants.forEach { v ->
                                val active = (selectedQuality ?: variants.firstOrNull()?.quality) == v.quality
                                Text(
                                    v.quality,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(Radius.sm))
                                        .background(if (active) colors.primary else colors.surface)
                                        .border(1.dp, if (active) colors.primary else colors.line, RoundedCornerShape(Radius.sm))
                                        .clickable { selectedQuality = v.quality }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = if (active) colors.onPrimary else colors.onSurface,
                                    fontSize = FontSize.EXTRA_SMALL,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    // ---- ذخیره برای پخشِ آفلاین ----
                    if (activeUrl != null) {
                        val offlineUri = LocalUriHandler.current
                        Text(
                            "↓  ذخیره برای پخشِ آفلاین",
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 4.dp)
                                .clickable { offlineUri.openUri(activeUrl) },
                            color = colors.primary,
                            fontSize = FontSize.SMALL,
                            fontWeight = FontWeight.SemiBold
                        )
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

                    // ---- تبِ ویدیو/فایل‌ها/آزمون/پرسش‌وپاسخ ----
                    var activeTab by remember(lesson?.id) { mutableStateOf(LessonTab.VIDEO) }
                    if (lesson != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LessonTabChip("ویدیو", activeTab == LessonTab.VIDEO) { activeTab = LessonTab.VIDEO }
                            if (lesson.resourceFiles.isNotEmpty() || lesson.subtitles.isNotEmpty()) {
                                LessonTabChip("فایل‌ها", activeTab == LessonTab.FILES) { activeTab = LessonTab.FILES }
                            }
                            if (lesson.hasQuiz) {
                                LessonTabChip("آزمون", activeTab == LessonTab.QUIZ) { activeTab = LessonTab.QUIZ }
                            }
                            LessonTabChip("پرسش‌وپاسخ", activeTab == LessonTab.QA) { activeTab = LessonTab.QA }
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
                                                Text(file.sizeLabel!!, color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
                                            }
                                        }
                                        Spacer(Modifier.height(6.dp))
                                    }
                                    lesson.subtitles.forEach { subtitle ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(Radius.sm))
                                                .background(colors.surfaceVariant)
                                                .clickable { uriHandler.openUri(subtitle.url) }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("زیرنویس: ${subtitle.language}", modifier = Modifier.weight(1f), color = colors.onSurface, fontSize = FontSize.SMALL)
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
                            LessonTab.QA -> {
                                LaunchedEffect(lesson.id) { viewModel.loadQuestions(lesson.id) }
                                LessonQaSection(
                                    questions = state.questionsByLesson[lesson.id].orEmpty(),
                                    loading = state.loadingQuestions,
                                    onSubmit = { content -> viewModel.submitQuestion(lesson.id, content) }
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
                        course.sections.forEachIndexed { sIdx, section ->
                            item {
                                Spacer(Modifier.height(8.dp))
                                val heading = if (section.title.contains("فصل")) section.title else "فصل $sIdx: ${section.title}"
                                Text(heading, fontWeight = FontWeight.Bold, color = colors.primary, fontSize = FontSize.REGULAR)
                            }
                            items(section.lessons.size) { idx ->
                                val l = section.lessons[idx]
                                LearnLessonRow(
                                    lesson = l,
                                    selected = l.id == state.selectedLessonId,
                                    onClick = { if (l.videoUrl != null) viewModel.selectLesson(l.id) },
                                    onToggleComplete = { if (!l.completed && l.videoUrl != null) viewModel.markComplete(l.id) }
                                )
                            }
                            item { Spacer(Modifier.height(4.dp)) }
                        }
                        if (course.isOnline && course.progressPercent < 100) {
                            item {
                                Text(
                                    "پس از تکمیلِ همه‌ی جلسات، آزمونِ پایانِ دوره فعال می‌شود.",
                                    color = colors.onSurfaceVariant, fontSize = FontSize.SMALL,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
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

/** پرسش‌وپاسخِ درس — mini-forum زیرِ ویدیو، هر کاربر می‌تواند سؤال بپرسد. */
@Composable
private fun LessonQaSection(
    questions: List<com.kazemieh.domain.academy.LessonQuestion>,
    loading: Boolean,
    onSubmit: (String) -> Unit
) {
    val colors = AppTheme.colors
    var text by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("سؤالت را بپرس…", fontSize = FontSize.SMALL) },
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "ارسال",
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(if (text.isNotBlank()) colors.primary else colors.surfaceVariant)
                    .clickable(enabled = text.isNotBlank()) { onSubmit(text.trim()); text = "" }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                color = colors.onPrimary, fontSize = FontSize.SMALL, fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(10.dp))
        when {
            loading -> Text("در حالِ بارگذاری…", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
            questions.isEmpty() -> Text("هنوز سؤالی پرسیده نشده. اولین نفر باش!", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
            else -> questions.forEach { q ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(Radius.md))
                        .background(colors.surface)
                        .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(30.dp).clip(androidx.compose.foundation.shape.CircleShape).background(colors.accentSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("س", color = colors.primary, fontWeight = FontWeight.Bold, fontSize = FontSize.EXTRA_SMALL)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(q.userName ?: "کاربر #${q.userId}", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.SMALL)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(q.content, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                }
            }
        }
    }
}

@Composable
private fun LearnLessonRow(lesson: Lesson, selected: Boolean, onClick: () -> Unit, onToggleComplete: () -> Unit) {
    val colors = AppTheme.colors
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.sm))
            .background(if (selected) colors.accentSoft else colors.surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            lesson.title,
            modifier = Modifier.weight(1f, fill = false).clickable { onClick() }.padding(vertical = 8.dp),
            color = colors.onSurface,
            fontSize = FontSize.REGULAR,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.Default.PlayArrow,
            contentDescription = null,
            tint = if (lesson.videoUrl != null) colors.primary else colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp).clickable { onClick() }
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (lesson.completed) colors.primary else colors.surface)
                .border(1.5.dp, if (lesson.completed) colors.primary else colors.line, RoundedCornerShape(6.dp))
                .clickable { onToggleComplete() },
            contentAlignment = Alignment.Center
        ) {
            if (lesson.completed) Icon(Icons.Default.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.weight(1f))
        if (lesson.resourceFiles.isNotEmpty()) {
            Text(
                "دانلود فایل",
                modifier = Modifier.clickable { uriHandler.openUri(lesson.resourceFiles.first().url) }.padding(vertical = 8.dp),
                color = colors.primary,
                fontSize = FontSize.SMALL,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
