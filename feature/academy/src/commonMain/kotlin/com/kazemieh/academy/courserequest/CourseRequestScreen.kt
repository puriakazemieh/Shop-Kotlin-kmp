package com.kazemieh.academy.courserequest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.component.CustomTextField
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.courserequest.CourseRequest
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseRequestScreen(
    navigateBack: () -> Unit
) {
    val viewModel = koinViewModel<CourseRequestViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CourseRequestEffect.ShowError -> messageBarState.addError(effect.message)
                CourseRequestEffect.Submitted -> {
                    title = ""
                    description = ""
                    messageBarState.addSuccess("درخواستِ شما ثبت شد.")
                }
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("درخواستِ دوره", fontSize = FontSize.MEDIUM, fontFamily = AppFont(), color = colors.onSurface) },
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
        ContentWithMessageBar(modifier = Modifier.padding(padding), messageBarState = messageBarState) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().responsiveMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "دوره‌ای که دوست دارید ساخته شود را پیشنهاد دهید. درخواست‌هایی که بیشترین لایک را بگیرند، زودتر تولید می‌شوند.",
                        fontSize = FontSize.SMALL,
                        fontFamily = AppFont(),
                        color = colors.onSurfaceVariant
                    )
                }

                // ---- فرمِ ثبتِ درخواست ----
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(colors.surface)
                            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("ثبتِ درخواستِ جدید", fontWeight = FontWeight.Bold, fontFamily = AppFont(), color = colors.onSurface, fontSize = FontSize.REGULAR)
                        CustomTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = "عنوانِ دوره (مثلاً: دوره‌ی Rust)",
                            modifier = Modifier.fillMaxWidth()
                        )
                        CustomTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "توضیحِ کوتاه (اختیاری)",
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default
                        )
                        val enabled = title.isNotBlank() && !state.submitting
                        Text(
                            if (state.submitting) "در حالِ ثبت…" else "ثبتِ درخواست",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Radius.button))
                                .background(if (enabled) colors.primary else colors.line)
                                .clickable(enabled = enabled) { viewModel.submit(title, description) }
                                .padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            color = colors.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppFont(),
                            fontSize = FontSize.REGULAR
                        )
                    }
                }

                when (val result = state.requests) {
                    is AppResult.Loading -> item { LoadingCard(Modifier.fillMaxWidth().height(160.dp)) }
                    is AppResult.Error -> item {
                        InfoCard(
                            title = stringResource(Resources.String.Oops),
                            subtitle = result.message,
                            image = Resources.Image.Cat
                        )
                    }
                    is AppResult.Success -> {
                        if (result.data.isEmpty()) {
                            item {
                                Text(
                                    "هنوز درخواستی ثبت نشده است. اولین نفر باشید!",
                                    fontSize = FontSize.SMALL,
                                    fontFamily = AppFont(),
                                    color = colors.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        } else {
                            items(result.data, key = { it.id }) { request ->
                                CourseRequestRow(request = request, onLike = { viewModel.toggleLike(request) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseRequestRow(request: CourseRequest, onLike: () -> Unit) {
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
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    request.title,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AppFont(),
                    color = colors.onSurface,
                    fontSize = FontSize.REGULAR
                )
                if (request.fulfilled) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "ساخته شد",
                        modifier = Modifier
                            .clip(RoundedCornerShape(Radius.sm))
                            .background(colors.ok.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        color = colors.ok,
                        fontSize = FontSize.EXTRA_SMALL,
                        fontFamily = AppFont(),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (!request.description.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(request.description!!, fontSize = FontSize.SMALL, fontFamily = AppFont(), color = colors.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "درخواست‌دهنده: ${request.requesterName ?: "کاربر"}",
                fontSize = FontSize.EXTRA_SMALL,
                fontFamily = AppFont(),
                color = colors.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(10.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(Radius.full))
                .background(if (request.liked) colors.accentSoft else colors.surfaceVariant)
                .clickable { onLike() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (request.liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "لایک",
                tint = if (request.liked) colors.primary else colors.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "${request.likeCount}",
                fontSize = FontSize.SMALL,
                fontFamily = AppFont(),
                fontWeight = FontWeight.Bold,
                color = if (request.liked) colors.primary else colors.onSurface
            )
        }
    }
}
