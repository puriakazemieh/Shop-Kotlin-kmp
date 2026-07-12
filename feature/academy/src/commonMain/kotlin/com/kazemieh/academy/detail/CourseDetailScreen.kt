package com.kazemieh.academy.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import com.seiko.imageloader.rememberImagePainter
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
                    item { CourseHeader(course) }
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
                    if (course.isOnline && course.sections.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text("سرفصل‌های دوره", fontWeight = FontWeight.ExtraBold, color = colors.onSurface, fontSize = FontSize.EXTRA_REGULAR)
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

/** جداکننده‌ی هزارگانِ تومان. */
private fun formatToman(value: Double): String {
    val n = value.toLong()
    val s = n.toString()
    val sb = StringBuilder()
    for ((i, c) in s.withIndex()) {
        if (i > 0 && (s.length - i) % 3 == 0) sb.append('٬')
        sb.append(c)
    }
    return sb.toString()
}

/** سرتیترِ دوره: کاورِ ۱۶:۹ + عنوان + چیپ‌ها + مدرس + قیمت + آمار + نوارِ پیشرفت. */
@Composable
private fun CourseHeader(course: CourseDetail) {
    val colors = AppTheme.colors
    Column {
        // ---- کاورِ دوره (۱۶:۹) ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(Radius.lg))
                .background(colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!course.thumbnailUrl.isNullOrBlank()) {
                Image(
                    painter = rememberImagePainter(course.thumbnailUrl!!),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("🎓", fontSize = FontSize.EXTRA_LARGE)
            }
        }
        Spacer(Modifier.height(14.dp))

        Text(course.title, fontWeight = FontWeight.ExtraBold, fontSize = FontSize.EXTRA_REGULAR, color = colors.onSurface)
        Spacer(Modifier.height(8.dp))
        CourseBadgesRow(course)

        if (!course.instructor.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text("مدرس: ${course.instructor}", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
        }
        val cohortStartDate = course.cohortStartDate
        if (!cohortStartDate.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text("شروعِ گروه: ${cohortStartDate.take(10)}", color = colors.gold, fontSize = FontSize.SMALL, fontWeight = FontWeight.SemiBold)
        }

        // ---- کارتِ آمار + قیمت ----
        Spacer(Modifier.height(14.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.md))
                .background(colors.surface)
                .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
                .padding(16.dp)
        ) {
            val lessonCount = course.sections.sumOf { it.lessons.size }
            if (course.isOnline) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    CourseStat("$lessonCount", "درس")
                    if (course.totalDurationSeconds > 0) {
                        val h = course.totalDurationSeconds / 3600
                        val m = (course.totalDurationSeconds % 3600) / 60
                        CourseStat(if (h > 0) "$h:${m.toString().padStart(2, '0')}" else "$m دقیقه", "زمان")
                    }
                    course.level?.let { lvl ->
                        val label = when (lvl) { "BEGINNER" -> "مقدماتی"; "INTERMEDIATE" -> "متوسط"; "ADVANCED" -> "پیشرفته"; else -> lvl }
                        CourseStat(label, "سطح")
                    }
                }
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = colors.line)
                Spacer(Modifier.height(14.dp))
            }
            // قیمت
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("قیمتِ دوره", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL, modifier = Modifier.weight(1f))
                when {
                    course.price <= 0.0 -> Text("رایگان", color = colors.ok, fontWeight = FontWeight.ExtraBold, fontSize = FontSize.MEDIUM)
                    course.discountedPrice != null && course.discountedPrice!! < course.price -> {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${formatToman(course.price)} تومان",
                                color = colors.onSurfaceVariant,
                                fontSize = FontSize.EXTRA_SMALL,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                            Text("${formatToman(course.discountedPrice!!)} تومان", color = colors.primary, fontWeight = FontWeight.ExtraBold, fontSize = FontSize.MEDIUM)
                        }
                    }
                    else -> Text("${formatToman(course.price)} تومان", color = colors.primary, fontWeight = FontWeight.ExtraBold, fontSize = FontSize.MEDIUM)
                }
            }
        }

        if (course.enrolled && course.isOnline && course.progressPercent > 0) {
            Spacer(Modifier.height(14.dp))
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
}

@Composable
private fun RowScope.CourseStat(value: String, label: String) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontWeight = FontWeight.ExtraBold, color = colors.onSurface, fontSize = FontSize.REGULAR, maxLines = 1)
        Spacer(Modifier.height(3.dp))
        Text(label, color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
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
    // قیمت را کنارِ دکمه نشان می‌دهیم مگر اینکه کاربر ثبت‌نام کرده باشد یا کلاس پر باشد
    val showPrice = !course.enrolled && !classFull
    Row(
        modifier = Modifier.fillMaxWidth().background(colors.surface).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (showPrice) {
            Column {
                if (course.price <= 0.0) {
                    Text("رایگان", color = colors.ok, fontWeight = FontWeight.ExtraBold, fontSize = FontSize.MEDIUM)
                } else {
                    val effective = course.discountedPrice?.takeIf { it < course.price } ?: course.price
                    if (effective < course.price) {
                        Text(
                            "${formatToman(course.price)}",
                            color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                    Text("${formatToman(effective)} تومان", color = colors.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = FontSize.REGULAR)
                }
            }
        }
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
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
            textAlign = TextAlign.Center,
            color = colors.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = FontSize.REGULAR
        )
    }
}
