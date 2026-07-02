package com.kazemieh.academy.list

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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.domain.academy.CourseSummary
import com.seiko.imageloader.rememberImagePainter
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(
    mine: Boolean,
    title: String,
    navigateBack: () -> Unit,
    navigateToCourse: (String) -> Unit
) {
    val viewModel = koinViewModel<CourseListViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors

    LaunchedEffect(mine) { viewModel.load(mine) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text(title, fontSize = FontSize.MEDIUM, color = colors.onSurface) },
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
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center), color = colors.primary
                )
                state.courses.isEmpty() -> Text(
                    text = if (mine) "هنوز در دوره‌ای ثبت‌نام نکرده‌اید." else "دوره‌ای موجود نیست.",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    color = colors.onSurfaceVariant
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
                ) {
                    items(state.courses) { course ->
                        CourseRow(course = course, onClick = { navigateToCourse(course.slug) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseRow(course: CourseSummary, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(Radius.sm)).background(colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!course.thumbnailUrl.isNullOrBlank()) {
                androidx.compose.foundation.Image(
                    painter = rememberImagePainter(course.thumbnailUrl!!),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(Radius.sm)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("🎓", fontSize = FontSize.LARGE)
            }
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(course.title, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
            if (!course.instructor.isNullOrBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(course.instructor!!, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
            }
            Spacer(Modifier.height(6.dp))
            Text("${course.lessonCount} درس", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
            if (course.enrolled) {
                Spacer(Modifier.height(6.dp))
                Text("ثبت‌نام‌شده", color = colors.ok, fontSize = FontSize.SMALL, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
