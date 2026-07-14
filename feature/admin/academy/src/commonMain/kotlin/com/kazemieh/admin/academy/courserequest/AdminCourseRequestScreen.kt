package com.kazemieh.admin.academy.courserequest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.courserequest.CourseRequest
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCourseRequestScreen(
    onBackClick: () -> Unit,
    embedded: Boolean = false
) {
    val viewModel = koinViewModel<AdminCourseRequestViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()
    var toDelete by remember { mutableStateOf<CourseRequest?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AdminCourseRequestEffect.ShowError -> messageBarState.addError(effect.message)
                is AdminCourseRequestEffect.ShowSuccess -> messageBarState.addSuccess(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = colors.surface,
        topBar = {
            if (!embedded) TopAppBar(
                title = { Text("درخواست‌های دوره", fontSize = FontSize.LARGE, color = colors.onSurface) },
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
            LazyColumn(
                modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column {
                        Text("درخواست‌های دوره‌ی کاربران", fontWeight = FontWeight.Bold, fontSize = FontSize.EXTRA_REGULAR, color = colors.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "اولویتِ تولیدِ دوره‌ی جدید معمولاً با بیشترین لایک‌هاست.",
                            fontSize = FontSize.SMALL,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
                when (val result = state.requests) {
                    is AppResult.Loading -> item { LoadingCard(Modifier.fillMaxWidth().height(200.dp)) }
                    is AppResult.Error -> item {
                        InfoCard(title = "خطا", subtitle = result.message, image = com.kazemieh.designsystem.Resources.Image.Cat)
                    }
                    is AppResult.Success -> {
                        if (result.data.isEmpty()) {
                            item {
                                Text(
                                    "هنوز درخواستی ثبت نشده است.",
                                    fontSize = FontSize.SMALL,
                                    color = colors.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        } else {
                            items(result.data, key = { it.id }) { request ->
                                AdminCourseRequestCard(request = request, onDelete = { toDelete = request })
                            }
                        }
                    }
                }
            }
        }
    }

    toDelete?.let { request ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            containerColor = colors.surface,
            title = { Text("حذفِ درخواست") },
            text = { Text("«${request.title}» حذف شود؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(request.id)
                    toDelete = null
                }) { Text("حذف", color = colors.sale, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun AdminCourseRequestCard(request: CourseRequest, onDelete: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // نشانِ لایک (سمتِ راست در RTL چون اولین فرزند است)
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(Radius.sm))
                .background(colors.accentSoft)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("${request.likeCount}", fontWeight = FontWeight.ExtraBold, fontSize = FontSize.REGULAR, color = colors.primary)
            Text("لایک", fontSize = FontSize.EXTRA_SMALL, color = colors.primary)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(request.title, fontWeight = FontWeight.Bold, fontSize = FontSize.REGULAR, color = colors.onSurface)
            Spacer(Modifier.height(3.dp))
            Text(
                "درخواست‌دهنده: ${request.requesterName ?: "کاربر"}",
                fontSize = FontSize.SMALL,
                color = colors.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = colors.sale)
        }
    }
}
