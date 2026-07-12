package com.kazemieh.admin.blog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image as ImageIcon
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.component.CarmillaFilterChip
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.domain.blog.BlogBlock
import com.seiko.imageloader.rememberImagePainter
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ManageBlogScreen(
    id: Long?,
    slug: String? = null,
    navigateBack: () -> Unit,
    viewModel: ManageBlogViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val mediaPicker = remember { MediaPicker() }

    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var thumbnailUrl by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("DRAFT") }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var isFeatured by remember { mutableStateOf(false) }
    var metaTitle by remember { mutableStateOf("") }
    var metaDescription by remember { mutableStateOf("") }

    var currentBlockIndexForMedia by remember { mutableStateOf<Int?>(null) }

    mediaPicker.InitializeMediaPicker { bytes, _ ->
        currentBlockIndexForMedia?.let { index ->
            viewModel.uploadBlockImage(index, bytes)
            currentBlockIndexForMedia = null
        }
    }

    LaunchedEffect(slug) {
        slug?.let { viewModel.loadBlog(it) }
    }

    LaunchedEffect(state.blog) {
        state.blog?.let {
            title = it.title
            summary = it.summary ?: ""
            thumbnailUrl = it.thumbnailUrl ?: ""
            status = it.status ?: "DRAFT"
            categoryId = it.category?.id
            isFeatured = it.isFeatured
            metaTitle = it.metaTitle ?: ""
            metaDescription = it.metaDescription ?: ""
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) navigateBack()
    }

    fun submit(targetStatus: String) {
        status = targetStatus
        viewModel.saveBlog(
            id = id ?: 0L,
            title = title,
            slug = slug ?: "",
            summary = summary,
            thumbnailUrl = thumbnailUrl,
            status = targetStatus,
            categoryId = categoryId,
            isFeatured = isFeatured,
            metaTitle = metaTitle,
            metaDescription = metaDescription
        )
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            EditorHeaderBar(
                title = if (id == null) "مقاله جدید" else "ویرایش مقاله",
                isSaving = state.isSaving,
                onBack = navigateBack,
                onSaveDraft = { submit("DRAFT") },
                onPublish = { submit("PUBLISHED") }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingCard(modifier = Modifier.fillMaxSize().padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .responsiveMaxWidth(com.kazemieh.designsystem.ContentWidth.readable)
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(Modifier.height(2.dp))

                // ---- عنوان و خلاصه ----
                BorderlessField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "عنوان مقاله را اینجا بنویسید...",
                    textStyle = TextStyle(
                        fontSize = FontSize.EXTRA_MEDIUM,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        fontFamily = AppFont()
                    ),
                    placeholderColor = colors.onSurfaceVariant
                )
                BorderlessField(
                    value = summary,
                    onValueChange = { summary = it },
                    placeholder = "خلاصه کوتاه مقاله (در کارت‌ها نمایش داده می‌شود)",
                    textStyle = TextStyle(
                        fontSize = FontSize.REGULAR,
                        color = colors.onSurfaceVariant,
                        fontFamily = AppFont()
                    ),
                    placeholderColor = colors.onSurfaceVariant
                )

                // ---- دسته‌بندی ----
                if (state.categories.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "دسته:",
                            fontFamily = AppFont(),
                            fontWeight = FontWeight.Bold,
                            fontSize = FontSize.REGULAR,
                            color = colors.onSurface,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        state.categories.forEach { cat ->
                            CarmillaFilterChip(
                                text = cat.name,
                                selected = categoryId == cat.id,
                                onClick = { categoryId = if (categoryId == cat.id) null else cat.id }
                            )
                        }
                    }
                }

                // ---- وضعیت + مقاله ویژه ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "وضعیت:",
                        fontFamily = AppFont(),
                        fontWeight = FontWeight.Bold,
                        fontSize = FontSize.REGULAR,
                        color = colors.onSurface
                    )
                    Spacer(Modifier.width(8.dp))
                    StatusChip("منتشرشده", status == "PUBLISHED") { status = "PUBLISHED" }
                    Spacer(Modifier.width(8.dp))
                    StatusChip("پیش‌نویس", status == "DRAFT") { status = "DRAFT" }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "مقاله ویژه",
                        fontFamily = AppFont(),
                        fontSize = FontSize.SMALL,
                        color = colors.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Switch(
                        checked = isFeatured,
                        onCheckedChange = { isFeatured = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = colors.primary,
                            uncheckedTrackColor = colors.surfaceVariant,
                            uncheckedBorderColor = colors.line
                        )
                    )
                }

                HorizontalDivider(color = colors.line)

                // ---- محتوای مقاله ----
                SectionHeader("محتوای مقاله")

                state.contentBlocks.forEachIndexed { index, block ->
                    ContentBlockCard(
                        index = index,
                        total = state.contentBlocks.size,
                        block = block,
                        onUpdate = { viewModel.updateBlock(index, it) },
                        onRemove = { viewModel.removeBlock(index) },
                        onMoveUp = { viewModel.moveBlock(index, true) },
                        onMoveDown = { viewModel.moveBlock(index, false) },
                        onPickImage = {
                            currentBlockIndexForMedia = index
                            mediaPicker.open()
                        }
                    )
                }

                AddBlockPanel(onAdd = { viewModel.addBlock(it) })

                HorizontalDivider(color = colors.line)

                // ---- تنظیمات سئو ----
                SectionHeader("تنظیمات سئو")
                SeoField(
                    label = "عنوان متا (Meta Title)",
                    value = metaTitle,
                    onValueChange = { metaTitle = it },
                    placeholder = "عنوان برای موتورهای جستجو"
                )
                SeoField(
                    label = "توضیحات متا (Meta Description)",
                    value = metaDescription,
                    onValueChange = { metaDescription = it },
                    placeholder = "توضیح کوتاه برای نتایج جستجو",
                    minLines = 3
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// =====================================================================================
//  هدر ویرایشگر: بازگشت · عنوان · ذخیره پیش‌نویس · انتشار
// =====================================================================================
@Composable
private fun EditorHeaderBar(
    title: String,
    isSaving: Boolean,
    onBack: () -> Unit,
    onSaveDraft: () -> Unit,
    onPublish: () -> Unit
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(Radius.sm))
                .background(colors.surface)
                .border(1.dp, colors.line, RoundedCornerShape(Radius.sm))
                .clickable(enabled = !isSaving) { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = colors.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            fontFamily = AppFont(),
            fontWeight = FontWeight.Bold,
            fontSize = FontSize.EXTRA_REGULAR,
            color = colors.onSurface
        )
        Spacer(Modifier.weight(1f))
        // ذخیره پیش‌نویس (Ghost)
        Text(
            text = "ذخیره پیش‌نویس",
            modifier = Modifier
                .clip(RoundedCornerShape(Radius.button))
                .background(colors.surfaceVariant)
                .clickable(enabled = !isSaving) { onSaveDraft() }
                .padding(horizontal = 12.dp, vertical = 9.dp),
            fontFamily = AppFont(),
            fontSize = FontSize.SMALL,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface
        )
        Spacer(Modifier.width(8.dp))
        // انتشار (Primary)
        Text(
            text = "انتشار",
            modifier = Modifier
                .clip(RoundedCornerShape(Radius.button))
                .background(colors.primary)
                .clickable(enabled = !isSaving) { onPublish() }
                .padding(horizontal = 18.dp, vertical = 9.dp),
            fontFamily = AppFont(),
            fontSize = FontSize.SMALL,
            fontWeight = FontWeight.Bold,
            color = colors.onPrimary
        )
    }
}

// =====================================================================================
//  کارت بلوک محتوا
// =====================================================================================
@Composable
private fun ContentBlockCard(
    index: Int,
    total: Int,
    block: BlogBlock,
    onUpdate: (BlogBlock) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onPickImage: () -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(Radius.md))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = blockTypeLabel(block),
                fontFamily = AppFont(),
                fontSize = FontSize.SMALL,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                BlockActionIcon(Icons.Default.ArrowUpward, enabled = index > 0, tint = colors.onSurfaceVariant, onClick = onMoveUp)
                BlockActionIcon(Icons.Default.ArrowDownward, enabled = index < total - 1, tint = colors.onSurfaceVariant, onClick = onMoveDown)
                BlockActionIcon(Icons.Default.Delete, enabled = true, tint = colors.sale, onClick = onRemove)
            }
        }

        when (block) {
            is BlogBlock.Header -> {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..3).forEach { level ->
                        LevelChip(level = level, selected = block.level == level) {
                            onUpdate(block.copy(level = level))
                        }
                    }
                }
                BorderlessField(
                    value = block.text,
                    onValueChange = { onUpdate(block.copy(text = it)) },
                    placeholder = "متن تیتر را بنویسید…",
                    textStyle = TextStyle(
                        fontSize = when (block.level) {
                            1 -> FontSize.EXTRA_MEDIUM
                            2 -> FontSize.MEDIUM
                            else -> FontSize.EXTRA_REGULAR
                        },
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        fontFamily = AppFont()
                    ),
                    placeholderColor = colors.onSurfaceVariant,
                    contentPadding = 0.dp
                )
            }

            is BlogBlock.Paragraph -> {
                BorderlessField(
                    value = block.text,
                    onValueChange = { onUpdate(block.copy(text = it)) },
                    placeholder = "متن پاراگراف خود را اینجا بنویسید...",
                    textStyle = TextStyle(
                        fontSize = FontSize.REGULAR,
                        color = colors.onSurface,
                        fontFamily = AppFont()
                    ),
                    placeholderColor = colors.onSurfaceVariant,
                    contentPadding = 0.dp
                )
            }

            is BlogBlock.Image -> {
                if (block.url.isNotEmpty()) {
                    Image(
                        painter = rememberImagePainter(block.url),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(Radius.sm)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.button))
                        .background(colors.accentSoft)
                        .clickable { onPickImage() }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CloudUpload, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (block.url.isEmpty()) "بارگذاری تصویر" else "تغییر تصویر",
                        fontFamily = AppFont(),
                        fontSize = FontSize.REGULAR,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primary
                    )
                }
            }

            is BlogBlock.Button -> {
                BorderlessField(
                    value = block.text,
                    onValueChange = { onUpdate(block.copy(text = it)) },
                    placeholder = "متن دکمه",
                    textStyle = TextStyle(fontSize = FontSize.REGULAR, fontWeight = FontWeight.SemiBold, color = colors.onSurface, fontFamily = AppFont()),
                    placeholderColor = colors.onSurfaceVariant,
                    contentPadding = 0.dp
                )
                BorderlessField(
                    value = block.url,
                    onValueChange = { onUpdate(block.copy(url = it)) },
                    placeholder = "لینک دکمه",
                    textStyle = TextStyle(fontSize = FontSize.REGULAR, color = colors.onSurfaceVariant, fontFamily = AppFont()),
                    placeholderColor = colors.onSurfaceVariant,
                    contentPadding = 0.dp
                )
            }

            is BlogBlock.ListBlock -> {
                BorderlessField(
                    value = block.items.joinToString("\n"),
                    onValueChange = { text -> onUpdate(BlogBlock.ListBlock(text.split("\n"))) },
                    placeholder = "هر آیتم را در یک خط بنویسید…",
                    textStyle = TextStyle(fontSize = FontSize.REGULAR, color = colors.onSurface, fontFamily = AppFont()),
                    placeholderColor = colors.onSurfaceVariant,
                    contentPadding = 0.dp
                )
            }

            is BlogBlock.Quote -> {
                BorderlessField(
                    value = block.text,
                    onValueChange = { onUpdate(block.copy(text = it)) },
                    placeholder = "متن نقل‌قول را بنویسید…",
                    textStyle = TextStyle(fontSize = FontSize.REGULAR, color = colors.onSurface, fontFamily = AppFont()),
                    placeholderColor = colors.onSurfaceVariant,
                    contentPadding = 0.dp
                )
            }

            is BlogBlock.Divider -> {
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = colors.line)
            }

            is BlogBlock.Unknown -> {
                Text(
                    text = "نوع بلوک پشتیبانی‌نشده: ${block.type}",
                    fontFamily = AppFont(),
                    fontSize = FontSize.SMALL,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

private fun blockTypeLabel(block: BlogBlock): String = when (block) {
    is BlogBlock.Header -> "تیتر"
    is BlogBlock.Paragraph -> "پاراگراف"
    is BlogBlock.Image -> "تصویر"
    is BlogBlock.Button -> "دکمه"
    is BlogBlock.ListBlock -> "فهرست"
    is BlogBlock.Quote -> "نقل‌قول"
    is BlogBlock.Divider -> "جداکننده"
    is BlogBlock.Unknown -> "نامشخص"
}

@Composable
private fun BlockActionIcon(
    icon: ImageVector,
    enabled: Boolean,
    tint: Color,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(30.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) tint else tint.copy(alpha = 0.35f),
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun LevelChip(level: Int, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        text = "H$level",
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.xs))
            .background(if (selected) colors.accentSoft else colors.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        fontFamily = AppFont(),
        fontSize = FontSize.EXTRA_SMALL,
        fontWeight = FontWeight.Bold,
        color = if (selected) colors.primary else colors.onSurfaceVariant
    )
}

// =====================================================================================
//  پنل افزودن بلوک جدید (بوردر خط‌چین + شبکه ۳ ستونه)
// =====================================================================================
@Composable
private fun AddBlockPanel(onAdd: (BlogBlock) -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .dashedBorder(colors.line, Radius.md)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "افزودن بلوک جدید",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontFamily = AppFont(),
            fontSize = FontSize.REGULAR,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
        // ردیف ۱ (راست→چپ): پاراگراف · تیتر · تصویر
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AddBlockTile(Modifier.weight(1f), Icons.AutoMirrored.Filled.Notes, "پاراگراف") { onAdd(BlogBlock.Paragraph("")) }
            AddBlockTile(Modifier.weight(1f), Icons.Default.Title, "تیتر") { onAdd(BlogBlock.Header("", 2)) }
            AddBlockTile(Modifier.weight(1f), Icons.Default.ImageIcon, "تصویر") { onAdd(BlogBlock.Image("")) }
        }
        // ردیف ۲ (راست→چپ): نقل‌قول · فهرست · دکمه
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AddBlockTile(Modifier.weight(1f), Icons.Default.FormatQuote, "نقل‌قول") { onAdd(BlogBlock.Quote("")) }
            AddBlockTile(Modifier.weight(1f), Icons.AutoMirrored.Filled.FormatListBulleted, "فهرست") { onAdd(BlogBlock.ListBlock(emptyList())) }
            AddBlockTile(Modifier.weight(1f), Icons.Default.SmartButton, "دکمه") { onAdd(BlogBlock.Button("", "")) }
        }
        // ردیف ۳: جداکننده (وسط‌چین)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Spacer(Modifier.weight(1f))
            AddBlockTile(Modifier.weight(1f), Icons.Default.HorizontalRule, "جداکننده") { onAdd(BlogBlock.Divider) }
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun AddBlockTile(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surfaceVariant)
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = colors.onSurface, modifier = Modifier.size(24.dp))
        Text(
            text = label,
            fontFamily = AppFont(),
            fontSize = FontSize.SMALL,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface
        )
    }
}

// =====================================================================================
//  فیلد سئو (برچسب بالا + کادر پرشده)
// =====================================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeoField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1
) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontFamily = AppFont(),
            fontSize = FontSize.SMALL,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radius.button)),
            placeholder = {
                Text(placeholder, fontFamily = AppFont(), fontSize = FontSize.REGULAR, color = colors.onSurfaceVariant)
            },
            textStyle = TextStyle(fontSize = FontSize.REGULAR, color = colors.onSurface, fontFamily = AppFont()),
            minLines = minLines,
            singleLine = minLines == 1,
            shape = RoundedCornerShape(Radius.button),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceVariant,
                unfocusedContainerColor = colors.surfaceVariant,
                disabledContainerColor = colors.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = colors.primary
            )
        )
    }
}

// =====================================================================================
//  اجزای کمکی
// =====================================================================================
@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        fontFamily = AppFont(),
        fontSize = FontSize.MEDIUM,
        fontWeight = FontWeight.Bold,
        color = AppTheme.colors.onSurface
    )
}

@Composable
private fun StatusChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.sm))
            .background(if (selected) colors.gold else colors.surfaceVariant)
            .then(if (selected) Modifier else Modifier.border(1.dp, colors.line, RoundedCornerShape(Radius.sm)))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        fontFamily = AppFont(),
        fontSize = FontSize.SMALL,
        fontWeight = FontWeight.SemiBold,
        color = if (selected) colors.onSecondary else colors.onSurface
    )
}

@Composable
private fun BorderlessField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textStyle: TextStyle,
    placeholderColor: Color,
    modifier: Modifier = Modifier,
    contentPadding: Dp = 4.dp
) {
    val colors = AppTheme.colors
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = contentPadding),
        textStyle = textStyle,
        cursorBrush = SolidColor(colors.primary),
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) {
                    Text(text = placeholder, style = textStyle.copy(color = placeholderColor))
                }
                inner()
            }
        }
    )
}

private fun Modifier.dashedBorder(color: Color, cornerRadius: Dp, strokeWidth: Dp = 1.dp): Modifier =
    this.drawBehind {
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(cornerRadius.toPx()),
            style = Stroke(
                width = strokeWidth.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
            )
        )
    }
