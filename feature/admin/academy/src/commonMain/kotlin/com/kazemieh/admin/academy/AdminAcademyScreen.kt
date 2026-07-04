package com.kazemieh.admin.academy

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.mutableStateListOf
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
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.academy.CourseDetail
import com.kazemieh.domain.academy.CourseSummary
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAcademyScreen(
    onBackClick: () -> Unit,
    embedded: Boolean = false
) {
    val viewModel = koinViewModel<AdminAcademyViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AdminAcademyEffect.ShowError -> messageBarState.addError(effect.message)
                is AdminAcademyEffect.ShowSuccess -> messageBarState.addSuccess(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = colors.surface,
        topBar = {
            if (!embedded) TopAppBar(
                title = { Text("مدیریتِ آموزشگاه", fontSize = FontSize.LARGE, color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface, titleContentColor = colors.onSurface)
            )
        }
    ) { padding ->
        ContentWithMessageBar(modifier = Modifier.padding(padding), messageBarState = messageBarState) {
            if (state.isLoading && state.courses.isEmpty()) {
                LoadingCard(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            "دوره‌های آموزشی", fontSize = FontSize.EXTRA_REGULAR,
                            fontWeight = FontWeight.ExtraBold, color = colors.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "دوره بساز، سپس با بازکردنِ آن، بخش و درس اضافه کن. لینکِ ویدیو باید مستقیم و قابلِ پخش باشد.",
                            fontSize = FontSize.SMALL, color = colors.onSurfaceVariant
                        )
                        Spacer(Modifier.height(14.dp))
                        AddCourseForm(onSubmit = { t, s, p, pid, ct, fmt, loc, cap, requiresProject ->
                            viewModel.createCourse(t, s, p, pid, ct, fmt, loc, cap, requiresProject)
                        })
                    }
                    items(state.courses) { course ->
                        CourseCard(
                            course = course,
                            expanded = state.expandedCourseId == course.id,
                            detail = if (state.expandedCourseId == course.id) state.expandedCourseDetail else null,
                            loadingDetail = state.loadingDetail && state.expandedCourseId == course.id,
                            quizCount = state.quizQuestionsByCourse[course.id]?.size ?: 0,
                            lessonQuizCounts = state.lessonQuizByLesson.mapValues { it.value.size },
                            waitlist = state.waitlistByCourse[course.id].orEmpty(),
                            projectSubmissions = state.projectSubmissionsByCourse[course.id].orEmpty(),
                            onToggle = {
                                viewModel.toggleExpand(course.id)
                                if (course.id != state.expandedCourseId) viewModel.loadProjectSubmissions(course.id)
                            },
                            onDelete = { viewModel.deleteCourse(course.id) },
                            onAddSection = { title -> viewModel.addSection(course.id, title) },
                            onAddLesson = { sectionId, title, url, minutes, free ->
                                viewModel.addLesson(course.id, sectionId, title, url, minutes, free)
                            },
                            onAddQuizQuestion = { passScore, text, options, correct ->
                                viewModel.addQuizQuestion(course.id, passScore, text, options, correct)
                            },
                            onAddLessonFile = { lessonId, name, url -> viewModel.addLessonFile(course.id, lessonId, name, url) },
                            onDeleteLessonFile = { lessonId, index -> viewModel.deleteLessonFile(course.id, lessonId, index) },
                            onAddLessonQuizQuestion = { lessonId, passScore, text, options, correct ->
                                viewModel.addLessonQuizQuestion(course.id, lessonId, passScore, text, options, correct)
                            },
                            onNotifyNextWaitlist = { viewModel.notifyNext(course.id) },
                            onReviewProject = { submissionId, status, feedback -> viewModel.reviewProject(course.id, submissionId, status, feedback) },
                            onUpdateCourse = { title, description, instructor, price, discountedPrice, requiresProject ->
                                viewModel.updateCourse(course.id, title, description, instructor, price, discountedPrice, requiresProject)
                            }
                        )
                    }
                }
            }
        }
    }
}

private val COURSE_TYPES = listOf("COURSE" to "دوره", "SEMINAR" to "سمینار", "WORKSHOP" to "کارگاه")
private val COURSE_FORMATS = listOf(
    "ONLINE_RECORDED" to "آنلاین (ضبط‌شده)",
    "ONLINE_LIVE" to "آنلاین (زنده)",
    "IN_PERSON" to "حضوری",
    "OFFLINE" to "آفلاین"
)

@Composable
private fun AddCourseForm(
    onSubmit: (title: String, slug: String, price: String, productId: String, courseType: String, format: String, location: String, capacity: String, requiresProject: Boolean) -> Unit
) {
    val colors = AppTheme.colors
    var title by remember { mutableStateOf("") }
    var slug by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var productId by remember { mutableStateOf("") }
    var courseType by remember { mutableStateOf("COURSE") }
    var format by remember { mutableStateOf("ONLINE_RECORDED") }
    var location by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var requiresProject by remember { mutableStateOf(false) }
    val isInPerson = format == "IN_PERSON" || format == "OFFLINE"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceVariant)
            .padding(14.dp)
    ) {
        Text("افزودنِ دوره‌ی جدید", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
        Spacer(Modifier.height(10.dp))
        AdminTextField(value = title, onValueChange = { title = it }, label = "عنوانِ دوره")
        Spacer(Modifier.height(8.dp))
        AdminTextField(value = slug, onValueChange = { slug = it }, label = "اسلاگ (لاتین، یکتا)")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                AdminTextField(value = price, onValueChange = { price = it }, label = "قیمت (تومان)")
            }
            Box(modifier = Modifier.weight(1f)) {
                AdminTextField(value = productId, onValueChange = { productId = it }, label = "شناسه‌ی محصول (اختیاری)")
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("نوعِ فعالیت", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        ChipSelector(options = COURSE_TYPES, selected = courseType, onSelect = { courseType = it })
        Spacer(Modifier.height(10.dp))
        Text("شکلِ برگزاری", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        ChipSelector(options = COURSE_FORMATS, selected = format, onSelect = { format = it })
        // فیلدهای مکان/ظرفیت فقط برای حضوری/آفلاین
        if (isInPerson) {
            Spacer(Modifier.height(8.dp))
            AdminTextField(value = location, onValueChange = { location = it }, label = "مکانِ برگزاری")
            Spacer(Modifier.height(8.dp))
            AdminTextField(value = capacity, onValueChange = { capacity = it }, label = "ظرفیتِ کلاس (نفر)")
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = requiresProject, onCheckedChange = { requiresProject = it })
            Text("گواهی نیازمندِ تأییدِ پروژه‌ی پایانی هم باشد", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "ساخت دوره",
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (title.isNotBlank() && slug.isNotBlank()) colors.primary else colors.line)
                .clickable(enabled = title.isNotBlank() && slug.isNotBlank()) {
                    onSubmit(title.trim(), slug.trim(), price.trim(), productId.trim(), courseType, format, location.trim(), capacity.trim(), requiresProject)
                    title = ""; slug = ""; price = ""; productId = ""; courseType = "COURSE"
                    format = "ONLINE_RECORDED"; location = ""; capacity = ""; requiresProject = false
                }
                .padding(horizontal = 16.dp, vertical = 11.dp),
            color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL
        )
    }
}

/** ردیفِ چیپ‌های انتخابِ تکیِ (value→label). */
@Composable
private fun ChipSelector(options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    val colors = AppTheme.colors
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { (value, label) ->
            val active = value == selected
            Text(
                label,
                fontSize = FontSize.EXTRA_SMALL,
                fontWeight = FontWeight.SemiBold,
                color = if (active) colors.onPrimary else colors.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (active) colors.primary else colors.surface)
                    .border(1.dp, if (active) colors.primary else colors.line, RoundedCornerShape(50))
                    .clickable { onSelect(value) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            )
        }
    }
}

@Composable
private fun CourseCard(
    course: CourseSummary,
    expanded: Boolean,
    detail: CourseDetail?,
    loadingDetail: Boolean,
    quizCount: Int,
    lessonQuizCounts: Map<Long, Int>,
    waitlist: List<com.kazemieh.domain.academy.AdminWaitlistEntry>,
    projectSubmissions: List<com.kazemieh.domain.academy.ProjectSubmission>,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onAddSection: (String) -> Unit,
    onAddLesson: (sectionId: Long, title: String, videoUrl: String, durationMinutes: String, isFreePreview: Boolean) -> Unit,
    onAddQuizQuestion: (passScore: String, text: String, options: List<String>, correctIndex: Int) -> Unit,
    onAddLessonFile: (lessonId: Long, name: String, url: String) -> Unit,
    onDeleteLessonFile: (lessonId: Long, index: Int) -> Unit,
    onAddLessonQuizQuestion: (lessonId: Long, passScore: String, text: String, options: List<String>, correctIndex: Int) -> Unit,
    onNotifyNextWaitlist: () -> Unit,
    onReviewProject: (submissionId: Long, status: String, feedback: String?) -> Unit,
    onUpdateCourse: (title: String, description: String, instructor: String, price: String, discountedPrice: String, requiresProject: Boolean) -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().clickable { onToggle() }, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(course.title, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
                Spacer(Modifier.height(3.dp))
                Text("${course.lessonCount} درس · اسلاگ: ${course.slug}", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
            }
            Box(
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(colors.sale.copy(alpha = 0.1f)).clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp), tint = colors.sale)
            }
        }

        if (expanded) {
            Spacer(Modifier.height(12.dp))
            when {
                loadingDetail -> Text("در حالِ بارگذاری…", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                detail != null -> {
                    EditCourseSection(detail = detail, onUpdate = onUpdateCourse)
                    Spacer(Modifier.height(10.dp))
                    detail.sections.forEach { section ->
                        Spacer(Modifier.height(8.dp))
                        Text(section.title, fontWeight = FontWeight.SemiBold, color = colors.onSurface, fontSize = FontSize.SMALL)
                        section.lessons.forEach { lesson ->
                            Text(
                                "· ${lesson.title}" + if (lesson.isFreePreview) " (پیش‌نمایش)" else "",
                                color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL,
                                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                            )
                            // ---- فایل‌های ضمیمه‌ی درس ----
                            if (lesson.resourceFiles.isNotEmpty()) {
                                Column(modifier = Modifier.padding(start = 16.dp, top = 2.dp)) {
                                    lesson.resourceFiles.forEachIndexed { idx, file ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("📎 ${file.name}", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL, modifier = Modifier.weight(1f))
                                            Text(
                                                "حذف", color = colors.sale, fontSize = FontSize.EXTRA_SMALL,
                                                modifier = Modifier.clickable { onDeleteLessonFile(lesson.id, idx) }.padding(4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            AddLessonFileForm(onSubmit = { name, url -> onAddLessonFile(lesson.id, name, url) })
                            LessonQuizBuilder(
                                lessonId = lesson.id,
                                hasQuiz = lesson.hasQuiz,
                                quizCount = lessonQuizCounts[lesson.id] ?: 0,
                                onAddQuestion = { passScore, text, options, correct -> onAddLessonQuizQuestion(lesson.id, passScore, text, options, correct) }
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        AddLessonForm(onSubmit = { title, url, minutes, free -> onAddLesson(section.id, title, url, minutes, free) })
                    }
                    Spacer(Modifier.height(10.dp))
                    AddSectionForm(onSubmit = onAddSection)
                    Spacer(Modifier.height(12.dp))
                    QuizBuilder(quizCount = quizCount, onAddQuestion = onAddQuizQuestion)
                    // ---- لیستِ انتظارِ کلاسِ حضوریِ پرشده (فقط اگر عضوی داشته باشد) ----
                    if (detail.isFull || waitlist.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        WaitlistSection(waitlist = waitlist, onNotifyNext = onNotifyNextWaitlist)
                    }
                    // ---- پروژه‌های پایانیِ ثبت‌شده (فقط دوره‌های پروژه‌محور) ----
                    if (detail.requiresProjectSubmission) {
                        Spacer(Modifier.height(12.dp))
                        ProjectSubmissionsSection(submissions = projectSubmissions, onReview = onReviewProject)
                    }
                }
            }
        }
    }
}

/** ویرایشِ فیلدهای پایه‌ی دوره (عنوان/توضیح/مدرس/قیمت/نیازِ پروژه) پس از ساخته‌شدن. */
@Composable
private fun EditCourseSection(detail: CourseDetail, onUpdate: (title: String, description: String, instructor: String, price: String, discountedPrice: String, requiresProject: Boolean) -> Unit) {
    val colors = AppTheme.colors
    var editing by remember(detail.id) { mutableStateOf(false) }
    Text(
        if (editing) "بستنِ ویرایش" else "ویرایشِ اطلاعاتِ دوره",
        color = colors.primary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clickable { editing = !editing }.padding(vertical = 4.dp)
    )
    if (editing) {
        var title by remember(detail.id) { mutableStateOf(detail.title) }
        var description by remember(detail.id) { mutableStateOf(detail.description.orEmpty()) }
        var instructor by remember(detail.id) { mutableStateOf(detail.instructor.orEmpty()) }
        var price by remember(detail.id) { mutableStateOf(detail.price.toString()) }
        var discountedPrice by remember(detail.id) { mutableStateOf(detail.discountedPrice?.toString().orEmpty()) }
        var requiresProject by remember(detail.id) { mutableStateOf(detail.requiresProjectSubmission) }
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surfaceVariant).padding(12.dp)
        ) {
            AdminTextField(value = title, onValueChange = { title = it }, label = "عنوانِ دوره")
            Spacer(Modifier.height(6.dp))
            AdminTextField(value = description, onValueChange = { description = it }, label = "توضیح")
            Spacer(Modifier.height(6.dp))
            AdminTextField(value = instructor, onValueChange = { instructor = it }, label = "مدرس")
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    AdminTextField(value = price, onValueChange = { price = it }, label = "قیمت")
                }
                Box(modifier = Modifier.weight(1f)) {
                    AdminTextField(value = discountedPrice, onValueChange = { discountedPrice = it }, label = "قیمتِ تخفیف‌خورده (اختیاری)")
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = requiresProject, onCheckedChange = { requiresProject = it })
                Text("گواهی نیازمندِ تأییدِ پروژه‌ی پایانی هم باشد", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "ذخیره‌ی تغییرات",
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.primary)
                    .clickable {
                        onUpdate(title.trim(), description.trim(), instructor.trim(), price.trim(), discountedPrice.trim(), requiresProject)
                        editing = false
                    }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                color = colors.onPrimary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WaitlistSection(waitlist: List<com.kazemieh.domain.academy.AdminWaitlistEntry>, onNotifyNext: () -> Unit) {
    val colors = AppTheme.colors
    val pending = waitlist.count { !it.notified }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant)
            .padding(12.dp)
    ) {
        Text("لیستِ انتظارِ کلاسِ پرشده", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.SMALL)
        Spacer(Modifier.height(4.dp))
        Text("$pending نفر در انتظار", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
        if (waitlist.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            waitlist.forEach { entry ->
                Text(
                    "کاربر #${entry.userId}" + if (entry.notified) " — مطلع شد ✓" else " — در انتظار",
                    color = if (entry.notified) colors.ok else colors.onSurfaceVariant,
                    fontSize = FontSize.EXTRA_SMALL,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "اطلاع‌رسانی به نفرِ بعدی",
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (pending > 0) colors.primary else colors.line)
                .clickable(enabled = pending > 0) { onNotifyNext() }
                .padding(horizontal = 14.dp, vertical = 9.dp),
            color = colors.onPrimary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold
        )
    }
}

/** فرمِ ساده‌ی تست‌ساز: افزودنِ سؤالِ چهارگزینه‌ای با علامت‌زدنِ گزینه‌ی درست + حدِ نصاب. */
@Composable
private fun QuizBuilder(
    quizCount: Int,
    onAddQuestion: (passScore: String, text: String, options: List<String>, correctIndex: Int) -> Unit
) {
    val colors = AppTheme.colors
    var passScore by remember { mutableStateOf("60") }
    var question by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "", "", "") }
    var correctIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant)
            .padding(12.dp)
    ) {
        Text("آزمونِ پایانِ دوره", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.SMALL)
        if (quizCount > 0) {
            Spacer(Modifier.height(4.dp))
            Text("$quizCount سؤال در این سشن اضافه شد", color = colors.ok, fontSize = FontSize.EXTRA_SMALL)
        }
        Spacer(Modifier.height(8.dp))
        AdminTextField(value = passScore, onValueChange = { passScore = it }, label = "حدِ نصابِ قبولی (٪)")
        Spacer(Modifier.height(6.dp))
        AdminTextField(value = question, onValueChange = { question = it }, label = "متنِ سؤال")
        Spacer(Modifier.height(6.dp))
        options.forEachIndexed { i, opt ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f)) {
                    AdminTextField(value = opt, onValueChange = { options[i] = it }, label = "گزینه‌ی ${i + 1}")
                }
                Spacer(Modifier.size(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = correctIndex == i, onCheckedChange = { if (it) correctIndex = i })
                    Text("درست", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(4.dp))
        }
        val canAdd = question.isNotBlank() && options.count { it.isNotBlank() } >= 2
        Text(
            "افزودنِ سؤال به آزمون",
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (canAdd) colors.primary else colors.line)
                .clickable(enabled = canAdd) {
                    onAddQuestion(passScore.trim(), question.trim(), options.toList(), correctIndex)
                    question = ""; options[0] = ""; options[1] = ""; options[2] = ""; options[3] = ""; correctIndex = 0
                }
                .padding(horizontal = 14.dp, vertical = 9.dp),
            color = colors.onPrimary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AddSectionForm(onSubmit: (String) -> Unit) {
    val colors = AppTheme.colors
    var title by remember { mutableStateOf("") }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f)) {
            AdminTextField(value = title, onValueChange = { title = it }, label = "عنوانِ بخشِ جدید")
        }
        Text(
            "افزودنِ بخش",
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (title.isNotBlank()) colors.primary else colors.line)
                .clickable(enabled = title.isNotBlank()) { onSubmit(title.trim()); title = "" }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            color = colors.onPrimary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AddLessonForm(onSubmit: (title: String, videoUrl: String, durationMinutes: String, isFreePreview: Boolean) -> Unit) {
    val colors = AppTheme.colors
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var free by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceVariant)
            .padding(10.dp)
    ) {
        AdminTextField(value = title, onValueChange = { title = it }, label = "عنوانِ درس")
        Spacer(Modifier.height(6.dp))
        AdminTextField(value = url, onValueChange = { url = it }, label = "لینکِ ویدیو (mp4/hls)")
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                AdminTextField(value = minutes, onValueChange = { minutes = it }, label = "مدت (دقیقه)")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = free, onCheckedChange = { free = it })
                Text("پیش‌نمایشِ رایگان", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "افزودنِ درس",
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (title.isNotBlank()) colors.primary else colors.line)
                .clickable(enabled = title.isNotBlank()) {
                    onSubmit(title.trim(), url.trim(), minutes.trim(), free)
                    title = ""; url = ""; minutes = ""; free = false
                }
                .padding(horizontal = 14.dp, vertical = 9.dp),
            color = colors.onPrimary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold
        )
    }
}

/** افزودنِ فایلِ ضمیمه با لینکِ مستقیم (بدونِ آپلود) — هم‌الگو با ثبتِ لینکِ ویدیو. */
@Composable
private fun AddLessonFileForm(onSubmit: (name: String, url: String) -> Unit) {
    val colors = AppTheme.colors
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            AdminTextField(value = name, onValueChange = { name = it }, label = "نامِ فایل (مثلاً جزوه)")
        }
        Box(modifier = Modifier.weight(1f)) {
            AdminTextField(value = url, onValueChange = { url = it }, label = "لینکِ فایل")
        }
        Text(
            "+", fontWeight = FontWeight.Bold, color = colors.onPrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (name.isNotBlank() && url.isNotBlank()) colors.primary else colors.line)
                .clickable(enabled = name.isNotBlank() && url.isNotBlank()) {
                    onSubmit(name.trim(), url.trim()); name = ""; url = ""
                }
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}

/** تست‌سازِ آزمونِ کوتاهِ یک درس (checkpoint) — هم‌الگو با QuizBuilderِ آزمونِ پایانِ دوره. */
@Composable
private fun LessonQuizBuilder(lessonId: Long, hasQuiz: Boolean, quizCount: Int, onAddQuestion: (passScore: String, text: String, options: List<String>, correctIndex: Int) -> Unit) {
    val colors = AppTheme.colors
    var expanded by remember(lessonId) { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp)) {
        Text(
            if (hasQuiz || quizCount > 0) "ویرایشِ آزمونِ این درس" else "افزودنِ آزمونِ کوتاه به این درس",
            color = colors.primary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { expanded = !expanded }.padding(vertical = 4.dp)
        )
        if (expanded) {
            var passScore by remember { mutableStateOf("60") }
            var question by remember { mutableStateOf("") }
            val options = remember { mutableStateListOf("", "", "", "") }
            var correctIndex by remember { mutableStateOf(0) }
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(colors.surfaceVariant).padding(10.dp)
            ) {
                if (quizCount > 0) {
                    Text("$quizCount سؤال در این سشن اضافه شد", color = colors.ok, fontSize = FontSize.EXTRA_SMALL)
                    Spacer(Modifier.height(6.dp))
                }
                AdminTextField(value = passScore, onValueChange = { passScore = it }, label = "حدِ نصابِ قبولی (٪)")
                Spacer(Modifier.height(6.dp))
                AdminTextField(value = question, onValueChange = { question = it }, label = "متنِ سؤال")
                Spacer(Modifier.height(6.dp))
                options.forEachIndexed { i, opt ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f)) {
                            AdminTextField(value = opt, onValueChange = { options[i] = it }, label = "گزینه‌ی ${i + 1}")
                        }
                        Spacer(Modifier.size(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = correctIndex == i, onCheckedChange = { if (it) correctIndex = i })
                            Text("درست", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
                val canAdd = question.isNotBlank() && options.count { it.isNotBlank() } >= 2
                Text(
                    "افزودنِ سؤال",
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (canAdd) colors.primary else colors.line)
                        .clickable(enabled = canAdd) {
                            onAddQuestion(passScore.trim(), question.trim(), options.toList(), correctIndex)
                            question = ""; options[0] = ""; options[1] = ""; options[2] = ""; options[3] = ""; correctIndex = 0
                        }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    color = colors.onPrimary, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** بررسیِ پروژه‌های پایانیِ ثبت‌شده‌ی کاربران — تأیید/رد + بازخورد. */
@Composable
private fun ProjectSubmissionsSection(submissions: List<com.kazemieh.domain.academy.ProjectSubmission>, onReview: (submissionId: Long, status: String, feedback: String?) -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surfaceVariant).padding(12.dp)
    ) {
        Text("پروژه‌های پایانیِ ثبت‌شده", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.SMALL)
        if (submissions.isEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text("هنوز پروژه‌ای ثبت نشده.", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
        }
        submissions.forEach { s ->
            Spacer(Modifier.height(8.dp))
            var feedback by remember(s.id) { mutableStateOf(s.mentorFeedback ?: "") }
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(colors.surface).padding(10.dp)
            ) {
                Text("کاربر #${s.userId}" + (s.userName?.let { " — $it" } ?: ""), fontWeight = FontWeight.SemiBold, color = colors.onSurface, fontSize = FontSize.EXTRA_SMALL)
                Text("لینک: ${s.fileUrl}", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
                Text(
                    "وضعیت: ${statusLabel(s.status)}",
                    color = statusColor(s.status, colors), fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                AdminTextField(value = feedback, onValueChange = { feedback = it }, label = "بازخوردِ مدرس")
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "تأیید", color = colors.ok, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(colors.ok.copy(alpha = 0.12f))
                            .clickable { onReview(s.id, "APPROVED", feedback.trim().ifBlank { null }) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                    Text(
                        "رد", color = colors.sale, fontSize = FontSize.EXTRA_SMALL, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(colors.sale.copy(alpha = 0.12f))
                            .clickable { onReview(s.id, "REJECTED", feedback.trim().ifBlank { null }) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

private fun statusLabel(status: String) = when (status) {
    "APPROVED" -> "تأییدشده"
    "REJECTED" -> "ردشده"
    else -> "در انتظارِ بررسی"
}

private fun statusColor(status: String, colors: com.kazemieh.designsystem.AppColors) = when (status) {
    "APPROVED" -> colors.ok
    "REJECTED" -> colors.sale
    else -> colors.onSurfaceVariant
}

@Composable
private fun AdminTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    val colors = AppTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant) },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
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
}
