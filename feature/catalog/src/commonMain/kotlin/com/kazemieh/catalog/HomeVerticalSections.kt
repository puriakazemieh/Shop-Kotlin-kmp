package com.kazemieh.catalog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.Resources
import com.kazemieh.domain.academy.CourseSummary
import com.kazemieh.domain.clinic.TherapistSummary
import com.kazemieh.domain.psychtest.PsychTestSummary
import com.seiko.imageloader.rememberImagePainter
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp

/** ردیفِ پیش‌نمایشِ دوره‌ها روی صفحه‌ی اصلی — درست مثلِ محصولات، نه فقط داخلِ پروفایل. */
@Composable
fun CourseHomeSection(
    courses: List<CourseSummary>,
    onSeeAll: () -> Unit,
    onCourseClick: (CourseSummary) -> Unit
) {
    Column {
        HomeSectionHeader(
            title = "دوره‌های آموزشی",
            modifier = Modifier.padding(horizontal = 16.dp),
            onSeeAll = onSeeAll
        )
        Spacer(Modifier.height(14.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(courses, key = { it.id }) { course ->
                CourseHomeCard(course = course, onClick = { onCourseClick(course) })
            }
        }
    }
}

@Composable
private fun CourseHomeCard(course: CourseSummary, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(Radius.lg))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.lg))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Image(
            modifier = Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(Radius.sm)),
            painter = rememberImagePainter(course.thumbnailUrl ?: ""),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = course.title,
            fontSize = FontSize.SMALL,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.height(36.dp)
        )
        Spacer(Modifier.height(6.dp))
        val price = course.discountedPrice ?: course.price
        Text(
            text = if (price <= 0.0) "رایگان" else stringResource(Resources.String.PriceFormat, price),
            fontSize = FontSize.EXTRA_SMALL,
            fontWeight = FontWeight.SemiBold,
            color = colors.primary
        )
    }
}

/** ردیفِ پیش‌نمایشِ درمانگرها روی صفحه‌ی اصلی. */
@Composable
fun TherapistHomeSection(
    therapists: List<TherapistSummary>,
    onSeeAll: () -> Unit,
    onTherapistClick: (TherapistSummary) -> Unit
) {
    Column {
        HomeSectionHeader(
            title = "مشاوره و روان‌شناسی",
            modifier = Modifier.padding(horizontal = 16.dp),
            onSeeAll = onSeeAll
        )
        Spacer(Modifier.height(14.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(therapists, key = { it.id }) { therapist ->
                TherapistHomeCard(therapist = therapist, onClick = { onTherapistClick(therapist) })
            }
        }
    }
}

@Composable
private fun TherapistHomeCard(therapist: TherapistSummary, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(Radius.lg))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.lg))
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(colors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            val photoUrl = therapist.photoUrl
            if (!photoUrl.isNullOrBlank()) {
                Image(
                    modifier = Modifier.size(56.dp).clip(CircleShape),
                    painter = rememberImagePainter(photoUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    therapist.name.take(1),
                    fontSize = FontSize.MEDIUM,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.primary
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            therapist.name,
            fontSize = FontSize.SMALL,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        val specialty = therapist.specialty
        if (!specialty.isNullOrBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                specialty,
                fontSize = FontSize.EXTRA_SMALL,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** ردیفِ پیش‌نمایشِ تست‌های روان‌شناسی روی صفحه‌ی اصلی. */
@Composable
fun PsychTestHomeSection(
    tests: List<PsychTestSummary>,
    onSeeAll: () -> Unit
) {
    Column {
        HomeSectionHeader(
            title = "تست‌های روان‌شناسی",
            modifier = Modifier.padding(horizontal = 16.dp),
            onSeeAll = onSeeAll
        )
        Spacer(Modifier.height(14.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tests, key = { it.id }) { test ->
                PsychTestHomeCard(test = test, onClick = onSeeAll)
            }
        }
    }
}

@Composable
private fun PsychTestHomeCard(test: PsychTestSummary, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .width(170.dp)
            .clip(RoundedCornerShape(Radius.lg))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.lg))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Text(
            test.title,
            fontSize = FontSize.SMALL,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.height(36.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "${test.questionCount} سؤال",
            fontSize = FontSize.EXTRA_SMALL,
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        val price = test.discountedPrice ?: test.price
        Text(
            text = if (price <= 0.0) "رایگان" else stringResource(Resources.String.PriceFormat, price),
            fontSize = FontSize.EXTRA_SMALL,
            fontWeight = FontWeight.SemiBold,
            color = colors.primary
        )
    }
}
