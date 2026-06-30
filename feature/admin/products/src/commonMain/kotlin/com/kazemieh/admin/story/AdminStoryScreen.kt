package com.kazemieh.admin.story

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kazemieh.common.AppResult
import androidx.compose.ui.window.Dialog
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.story.Story
import com.kazemieh.domain.story.StoryMediaType
import com.kazemieh.admin.products.MediaPicker
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminStoryScreen(
    navigateBack: () -> Unit,
    embedded: Boolean = false
) {
    val viewModel = koinViewModel<AdminStoryViewModel>()
    val state by viewModel.state.collectAsState()
    val messageBarState = rememberMessageBarState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val mediaPicker = remember { MediaPicker() }

    var selectedMediaBytes by remember { mutableStateOf<ByteArray?>(null) }
    var isVideo by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    mediaPicker.InitializeMediaPicker(
        onMediaSelect = { bytes, video ->
            selectedMediaBytes = bytes
            isVideo = video
            showCreateDialog = true
        }
    )

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AdminStoryEffect.ShowError -> messageBarState.addError(effect.message)
                is AdminStoryEffect.ShowSuccess -> messageBarState.addSuccess(effect.message)
            }
        }
    }

    if (showCreateDialog && selectedMediaBytes != null) {
        CreateStoryDialog(
            mediaBytes = selectedMediaBytes,
            onPickMedia = { mediaPicker.open() },
            onDismiss = {
                showCreateDialog = false
                selectedMediaBytes = null
            },
            onConfirm = { title, productId ->
                viewModel.handleIntent(
                    AdminStoryIntent.CreateStory(
                        selectedMediaBytes!!,
                        if (isVideo) StoryMediaType.VIDEO else StoryMediaType.IMAGE,
                        productId,
                        title
                    )
                )
                showCreateDialog = false
                selectedMediaBytes = null
            }
        )
    }

    Scaffold(
        topBar = {
            if (!embedded) TopAppBar(
                title = { Text("مدیریت استوری‌ها") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            modifier = Modifier.graphicsLayer { rotationY = if (isRtl) 180f else 0f },
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->
        val colors = AppTheme.colors
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // هدر: عنوان + دکمه‌ی افزودنِ استوری (مطابق اسپک)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "استوری‌های فعال",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = FontSize.EXTRA_REGULAR,
                    color = colors.onSurface
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(11.dp))
                        .background(colors.primary)
                        .clickable { mediaPicker.open() }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(painterResource(Resources.Icon.Plus), contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(15.dp))
                    Text("استوری جدید", color = colors.onPrimary, fontSize = FontSize.SMALL, fontWeight = FontWeight.Bold)
                }
            }
            ContentWithMessageBar(messageBarState = messageBarState) {
                PullToRefreshBox(
                    isRefreshing = state.isLoading,
                    onRefresh = { viewModel.handleIntent(AdminStoryIntent.Refresh) }
                ) {
                    if (state.stories.isEmpty() && !state.isLoading) {
                        InfoCard(
                            image = Resources.Image.Cat,
                            title = stringResource(Resources.String.Oops),
                            subtitle = stringResource(Resources.String.NothingHere)
                        )
                    } else {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.stories) { story ->
                                AdminStoryCircle(
                                    story = story,
                                    onDelete = { viewModel.handleIntent(AdminStoryIntent.DeleteStory(story.id)) }
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
fun CreateStoryDialog(
    mediaBytes: ByteArray?,
    onPickMedia: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (title: String?, productId: Long?) -> Unit
) {
    val colors = AppTheme.colors
    var title by remember { mutableStateOf("") }
    var productId by remember { mutableStateOf("") }
    var linkToProduct by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.lg))
                .background(colors.surface)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("استوری جدید", fontWeight = FontWeight.ExtraBold, fontSize = FontSize.EXTRA_MEDIUM, color = colors.onSurface)
            Spacer(Modifier.height(16.dp))

            // کادرِ آپلود / پیش‌نمایش
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(Radius.md))
                    .background(colors.surfaceVariant)
                    .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
                    .clickable { onPickMedia() },
                contentAlignment = Alignment.Center
            ) {
                if (mediaBytes != null) {
                    AsyncImage(
                        model = mediaBytes,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(Radius.md)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("آپلود تصویر یا ویدیو استوری", fontSize = FontSize.SMALL, color = colors.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("عنوان استوری", fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL, color = colors.onSurface)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("مثلاً حراج پاییز", fontSize = FontSize.SMALL, color = colors.onSurfaceVariant) },
                shape = RoundedCornerShape(Radius.sm),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surfaceVariant,
                    unfocusedContainerColor = colors.surfaceVariant,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.line,
                    cursorColor = colors.primary,
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface
                )
            )

            Spacer(Modifier.height(16.dp))
            Text("لینک استوری به", fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL, color = colors.onSurface)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StoryLinkChip("بدون لینک", selected = !linkToProduct) { linkToProduct = false; productId = "" }
                StoryLinkChip("محصول", selected = linkToProduct) { linkToProduct = true }
            }
            if (linkToProduct) {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = productId,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) productId = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("شناسه محصول", fontSize = FontSize.SMALL, color = colors.onSurfaceVariant) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(Radius.sm),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surfaceVariant,
                        unfocusedContainerColor = colors.surfaceVariant,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.line,
                        cursorColor = colors.primary,
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface
                    )
                )
            }

            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "انتشار استوری",
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(Radius.button))
                        .background(if (mediaBytes != null) colors.primary else colors.line)
                        .clickable(enabled = mediaBytes != null) {
                            onConfirm(title.ifBlank { null }, if (linkToProduct) productId.toLongOrNull() else null)
                        }
                        .padding(vertical = 13.dp),
                    textAlign = TextAlign.Center,
                    color = colors.onPrimary,
                    fontSize = FontSize.REGULAR,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "انصراف",
                    modifier = Modifier
                        .weight(0.5f)
                        .clip(RoundedCornerShape(Radius.button))
                        .border(1.dp, colors.line, RoundedCornerShape(Radius.button))
                        .clickable { onDismiss() }
                        .padding(vertical = 13.dp),
                    textAlign = TextAlign.Center,
                    color = colors.onSurfaceVariant,
                    fontSize = FontSize.REGULAR,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StoryLinkChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .then(
                if (selected) Modifier.background(colors.primary)
                else Modifier.background(colors.surface).border(1.dp, colors.line, RoundedCornerShape(11.dp))
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 9.dp),
        fontSize = FontSize.SMALL,
        fontWeight = FontWeight.Bold,
        color = if (selected) colors.onPrimary else colors.onSurfaceVariant
    )
}

@Composable
fun AdminStoryCircle(
    story: Story,
    onDelete: () -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.width(74.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopStart) {
            // حلقه‌ی استوری
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(colors.gold, colors.primary)))
                    .padding(2.5.dp)
                    .clip(CircleShape)
                    .background(colors.surface)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // حرفِ اولِ عنوان به‌عنوان جایگزین وقتی تصویر نیست (مطابق اسپک)
                Text(
                    text = story.title?.trim()?.firstOrNull()?.toString() ?: "ک",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = FontSize.EXTRA_MEDIUM,
                    color = colors.onSurfaceVariant
                )
                AsyncImage(
                    model = story.mediaUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            // دکمه‌ی حذف
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(colors.sale)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Text("×", color = Color.White, fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = story.title ?: "بدون عنوان",
            fontSize = FontSize.EXTRA_SMALL,
            color = colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
