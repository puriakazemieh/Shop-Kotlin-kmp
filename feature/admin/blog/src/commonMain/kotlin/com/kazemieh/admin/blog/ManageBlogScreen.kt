package com.kazemieh.admin.blog

import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.component.CarmillaFilterChip
import com.kazemieh.designsystem.component.CustomTextField
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.domain.blog.Blog
import com.kazemieh.domain.blog.BlogBlock
import com.seiko.imageloader.rememberImagePainter
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBlogScreen(
    id: Long?,
    slug: String? = null,
    navigateBack: () -> Unit,
    viewModel: ManageBlogViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val mediaPicker = remember { MediaPicker() }

    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var thumbnailUrl by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("PUBLISHED") }
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
            status = it.status ?: "PUBLISHED"
            categoryId = it.category?.id
            isFeatured = it.isFeatured
            metaTitle = it.metaTitle ?: ""
            metaDescription = it.metaDescription ?: ""
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) navigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (id == null) "ساخت مقاله" else "ویرایش مقاله") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveBlog(
                            id = id ?: 0L,
                            title = title,
                            slug = slug ?: "",
                            summary = summary,
                            thumbnailUrl = thumbnailUrl,
                            status = status,
                            categoryId = categoryId,
                            isFeatured = isFeatured,
                            metaTitle = metaTitle,
                            metaDescription = metaDescription
                        )
                    }) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingCard(modifier = Modifier.fillMaxSize())
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ... Basic Info Section ...
                Text("اطلاعات پایه", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                CustomTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "عنوان",
                    modifier = Modifier.fillMaxWidth()
                )
                CustomTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    placeholder = "خلاصه",
                    modifier = Modifier.fillMaxWidth()
                )

                // ... Blocks Editor Section ...
                HorizontalDivider()
                Text("بلوک‌های محتوا", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                
                state.contentBlocks.forEachIndexed { index, block ->
                    BlockItem(
                        index = index,
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
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.addBlock(BlogBlock.Header("", 1)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Title, null)
                        Text("H")
                    }
                    Button(
                        onClick = { viewModel.addBlock(BlogBlock.Paragraph("")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Notes, null)
                        Text("P")
                    }
                    Button(
                        onClick = { viewModel.addBlock(BlogBlock.Image("")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Image, null)
                        Text("تصویر")
                    }
                }

                HorizontalDivider()
                Text("تنظیمات و سئو", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                CustomTextField(
                    value = thumbnailUrl,
                    onValueChange = { thumbnailUrl = it },
                    placeholder = "آدرس تصویر شاخص",
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (thumbnailUrl.isNotEmpty()) {
                    Image(
                        painter = rememberImagePainter(thumbnailUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(Radius.xs)),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFeatured, onCheckedChange = { isFeatured = it })
                    Text("مطلب ویژه", modifier = Modifier.padding(start = 8.dp))
                }

                Text("وضعیت")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CarmillaFilterChip(
                        text = "منتشرشده",
                        selected = status == "PUBLISHED",
                        onClick = { status = "PUBLISHED" }
                    )
                    CarmillaFilterChip(
                        text = "پیش‌نویس",
                        selected = status == "DRAFT",
                        onClick = { status = "DRAFT" }
                    )
                }

                CustomTextField(
                    value = metaTitle,
                    onValueChange = { metaTitle = it },
                    placeholder = "عنوان متا",
                    modifier = Modifier.fillMaxWidth()
                )
                CustomTextField(
                    value = metaDescription,
                    onValueChange = { metaDescription = it },
                    placeholder = "توضیحات متا",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun BlockItem(
    index: Int,
    block: BlogBlock,
    onUpdate: (BlogBlock) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onPickImage: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (block) {
                        is BlogBlock.Header -> "تیتر H${block.level}"
                        is BlogBlock.Paragraph -> "پاراگراف"
                        is BlogBlock.Image -> "تصویر"
                        is BlogBlock.Unknown -> "نامشخص"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    IconButton(onClick = onMoveUp) { Icon(Icons.Default.ArrowUpward, null, Modifier.size(18.dp)) }
                    IconButton(onClick = onMoveDown) { Icon(Icons.Default.ArrowDownward, null, Modifier.size(18.dp)) }
                    IconButton(onClick = onRemove) { Icon(Icons.Default.Delete, null, Modifier.size(18.dp), tint = AppTheme.colors.sale) }
                }
            }

            when (block) {
                is BlogBlock.Header -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(modifier = Modifier.weight(1f)) {
                            (1..3).forEach { level ->
                                FilterChip(
                                    selected = block.level == level,
                                    onClick = { onUpdate(block.copy(level = level)) },
                                    label = { Text("H$level") },
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = block.text,
                        onValueChange = { onUpdate(block.copy(text = it)) },
                        placeholder = { Text("متن تیتر") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.titleLarge
                    )
                }
                is BlogBlock.Paragraph -> {
                    OutlinedTextField(
                        value = block.text,
                        onValueChange = { onUpdate(block.copy(text = it)) },
                        placeholder = { Text("متن پاراگراف") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp)
                    )
                }
                is BlogBlock.Image -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (block.url.isNotEmpty()) {
                            Image(
                                painter = rememberImagePainter(block.url),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Button(
                            onClick = onPickImage,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CloudUpload, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (block.url.isEmpty()) "بارگذاری تصویر" else "تغییر تصویر")
                        }
                    }
                }
                is BlogBlock.Unknown -> {
                    Text("نوع بلوک پشتیبانی‌نشده: ${block.type}")
                }
            }
        }
    }
}
