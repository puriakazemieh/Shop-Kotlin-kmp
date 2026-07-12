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
import androidx.compose.foundation.layout.FlowRow
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
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.details.component.ProductReviewsSection
import com.kazemieh.domain.academy.CourseDetail
import com.kazemieh.domain.academy.Lesson
import kotlinx.coroutines.flow.collectLatest
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
    val messageBarState = rememberMessageBarState()

    LaunchedEffect(slug) { viewModel.load(slug) }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CourseDetailEffect.ShowError -> messageBarState.addError(effect.message)
                is CourseDetailEffect.ShowSuccess -> messageBarState.addSuccess(effect.message)
            }
        }
    }

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
                    joiningWaitlist = state.isJoiningWaitlist,
                    onEnroll = { viewModel.enroll() },
                    onContinue = { navigateToLearn(course.slug) },
                    onJoinWaitlist = { viewModel.joinWaitlist() }
                )
            }
        }
    ) { padding ->
        ContentWithMessageBar(modifier = Modifier.padding(padding), messageBarState = messageBarState) {
        Box(modifier = Modifier.fillMaxSize()) {
            val course = state.course
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.primary)
                course != null -> LazyColumn(
                    modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
                ) {
                    item {
                        Text(course.title, fontWeight = FontWeight.ExtraBold, fontSize = FontSize.EXTRA_REGULAR, color = colors.onSurface)
                        Spacer(Modifier.height(8.dp))
                        CourseBadgesRow(course)
                        if (!course.instructor.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text("مدرس: ${course.instructor}", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                        }
                        val cohortStartDate = course.cohortStartDate
                        if (!cohortStartDate.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text("شروعِ گروه: ${cohortStartDate.take(10)}", color = colors.gold, fontSize = FontSize.SMALL, fontWeight = FontWeight.SemiBold)
                        }
                        if (course.enrolled && course.isOnline && course.progressPercent > 0) {
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
                    // ---- دوره‌ی حضوری/آفلاین: کارتِ مکان و ظرفیت (به‌جای فهرستِ درس‌ها) ----
                    if (!course.isOnline) {
                        item { InPersonInfoCard(course) }
                    }
                    if (!course.instructorBio.isNullOrBlank() || course.instructorSkills.isNotEmpty()) {
                        item { InstructorCard(course) }
                    }
                    // ---- کدِ تخفیفِ اختصاصیِ مدرس (اگر تنظیم شده باشد) ----
                    if (!course.instructorDiscountCode.isNullOrBlank()) {
                        item { InstructorDiscountCard(course.instructorDiscountCode!!) }
                    }
                    if (!course.description.isNullOrBlank()) {
                        item {
                            Text(course.description!!, color = colors.onSurfaceVariant, fontSize = FontSize.REGULAR)
                        }
                    }
                    // ---- «این دوره شامل چیست» ----
                    if (course.isOnline && (course.totalDurationSeconds > 0 || course.resourceFileCount > 0)) {
                        item { CourseIncludesCard(course) }
                    }
                    // ---- سرفصل‌ها فقط برای دوره‌های آنلاین (محتوایِ قابلِ‌پخش) ----
                    if (course.isOnline) {
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
                    // ---- گارانتیِ بازگشتِ وجه (فقط دوره‌های آنلاینِ ثبت‌نام‌شده) ----
                    if (course.enrolled && course.isOnline) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                if (state.isRequestingRefund) "در حالِ ثبتِ درخواست…" else "درخواستِ بازگشتِ وجه (گارانتیِ ۷ روزه)",
                                modifier = Modifier.clickable(enabled = !state.isRequestingRefund) { viewModel.requestRefund(null) }.padding(vertical = 4.dp),
                                color = colors.onSurfaceVariant, fontSize = FontSize.SMALL, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            )
                        }
                    }
                    // ---- نظرِ شاگردان (فقط اگر دوره به محصول لینک شده باشد) ----
                    course.productId?.let { pid ->
                        item {
                            Spacer(Modifier.height(16.dp))
                            ProductReviewsSection(productId = pid, title = "نظرِ شاگردان")
                        }
                    }
                }
            }
        }
        }
    }
}

/** ردیفِ چیپ‌های نوع/فرمت/سطح + نشان‌های ویژه (بازار کار / بروزرسانی رایگان). */
@Composable
private fun CourseBadgesRow(course: CourseDetail) {
    val colors = AppTheme.colors
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Chip(course.courseType.label, colors.accentSoft, colors.primary)
        Chip(course.format.label, colors.surfaceVariant, colors.onSurfaceVariant)
        course.level?.let { lvl ->
            val label = when (lvl) { "BEGINNER" -> "مقدماتی"; "INTERMEDIATE" -> "متوسط"; "ADVANCED" -> "پیشرفته"; else -> lvl }
            Chip(label, colors.surfaceVariant, colors.onSurfaceVariant)
        }
        if (course.jobMarketBadge) Chip("پل ورود به بازار کار", colors.gold.copy(alpha = 0.15f), colors.gold)
        if (course.freeUpdateBadge) Chip("بروزرسانی رایگان", colors.ok.copy(alpha = 0.15f), colors.ok)
    }
}

@Composable
private fun Chip(text: String, bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color) {
    Text(
        text,
        fontSize = FontSize.EXTRA_SMALL,
        fontWeight = FontWeight.SemiBold,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.full))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

/** کارتِ اطلاعاتِ دوره‌ی حضوری/آفلاین: مکان و ظرفیت. */
@Composable
private fun InPersonInfoCard(course: CourseDetail) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("اطلاعاتِ برگزاری", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
        if (!course.location.isNullOrBlank()) {
            InfoLine("مکان", course.location!!)
        }
        val cap = course.capacity
        if (cap != null) {
            val remaining = course.seatsRemaining ?: (cap - course.seatsTaken).coerceAtLeast(0)
            InfoLine("ظرفیت", "$cap نفر")
            InfoLine("صندلی‌های باقی‌مانده", if (remaining > 0) "$remaining نفر" else "تکمیل")
        }
    }
}

@Composable
private fun InstructorCard(course: CourseDetail) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("درباره‌ی مدرس", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
        if (!course.instructorBio.isNullOrBlank()) {
            Text(course.instructorBio!!, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
        }
        if (course.instructorSkills.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                course.instructorSkills.forEach { Chip(it, colors.surfaceVariant, colors.onSurfaceVariant) }
            }
        }
    }
}

/** کدِ تخفیفِ اختصاصیِ مدرس — مثلاً برای پرداختِ محصولِ فروشگاهیِ لینک‌شده به این دوره. */
@Composable
private fun InstructorDiscountCard(code: String) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.gold.copy(alpha = 0.1f))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("کدِ تخفیفِ اختصاصیِ مدرس", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.SMALL)
            Spacer(Modifier.height(4.dp))
            Text("این کد را هنگامِ خرید وارد کن", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
        }
        Text(
            code, fontWeight = FontWeight.ExtraBold, color = colors.gold, fontSize = FontSize.REGULAR,
            modifier = Modifier.clip(RoundedCornerShape(Radius.sm)).background(colors.surface).padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/** جعبه‌ی «این دوره شامل چیست» — مجموعِ مدتِ ویدیوها و تعدادِ فایل‌های ضمیمه. */
@Composable
private fun CourseIncludesCard(course: CourseDetail) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .padding(16.dp)
    ) {
        Text("این دوره شامل چیست", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
        Spacer(Modifier.height(10.dp))
        if (course.totalDurationSeconds > 0) {
            val hours = course.totalDurationSeconds / 3600
            val minutes = (course.totalDurationSeconds % 3600) / 60
            val label = if (hours > 0) "$hours ساعت و $minutes دقیقه ویدیوی آموزشی" else "$minutes دقیقه ویدیوی آموزشی"
            InfoLine("مدتِ کل", label)
        }
        if (course.resourceFileCount > 0) {
            Spacer(Modifier.height(6.dp))
            InfoLine("فایل‌های ضمیمه", "${course.resourceFileCount} فایل")
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    val colors = AppTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
        Spacer(Modifier.width(8.dp))
        Text(value, color = colors.onSurface, fontWeight = FontWeight.SemiBold, fontSize = FontSize.SMALL)
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
    joiningWaitlist: Boolean,
    onEnroll: () -> Unit,
    onContinue: () -> Unit,
    onJoinWaitlist: () -> Unit
) {
    val colors = AppTheme.colors
    // برای دوره‌ی حضوری/آفلاینِ پرشده، ثبت‌نام غیرفعال است و به‌جایش لیستِ انتظار پیشنهاد می‌شود.
    val classFull = course.isFull && !course.enrolled
    val label = when {
        enrolling -> "در حال ثبت‌نام…"
        joiningWaitlist -> "در حال پیوستن به لیستِ انتظار…"
        classFull && course.onWaitlist -> "در لیستِ انتظار هستید ✓"
        classFull -> "پیوستن به لیستِ انتظار"
        course.enrolled && course.isOnline -> "ادامه‌ی یادگیری"
        course.enrolled -> "ثبت‌نامِ شما تأیید شده"
        !course.isOnline -> "رزرو صندلی"
        else -> "ثبت‌نام در دوره"
    }
    val clickable = when {
        enrolling || joiningWaitlist -> false
        classFull -> !course.onWaitlist
        course.enrolled -> course.isOnline
        else -> true
    }
    val bg = if (classFull && course.onWaitlist) colors.onSurfaceVariant else colors.primary
    Box(modifier = Modifier.fillMaxWidth().background(colors.surface).padding(16.dp)) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.button))
                .background(bg)
                .clickable(enabled = clickable) {
                    when {
                        classFull -> onJoinWaitlist()
                        course.enrolled && course.isOnline -> onContinue()
                        else -> onEnroll()
                    }
                }
                .padding(vertical = 15.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = colors.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = FontSize.REGULAR
        )
    }
}
