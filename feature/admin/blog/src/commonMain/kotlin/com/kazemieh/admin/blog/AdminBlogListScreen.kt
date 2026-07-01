package com.kazemieh.admin.blog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize
import com.kazemieh.domain.blog.Blog
import com.kazemieh.domain.blog.BlogCategory
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.seiko.imageloader.rememberImagePainter
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBlogListScreen(
    navigateToManageBlog: (Long?, String?) -> Unit,
    navigateToManageCategory: (Long?) -> Unit,
    navigateBack: () -> Unit,
    embedded: Boolean = false,
    viewModel: AdminBlogListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val messageBarState = rememberMessageBarState()

    // دیالوگ‌های محلی: ساخت/ویرایش دسته‌بندی و تأییدِ حذف
    var showCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<BlogCategory?>(null) }
    var blogToDelete by remember { mutableStateOf<Blog?>(null) }
    var categoryToDelete by remember { mutableStateOf<BlogCategory?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AdminBlogListEffect.ShowError -> messageBarState.addError(effect.message)
                is AdminBlogListEffect.BlogDeleted -> messageBarState.addSuccess("مقاله حذف شد")
                is AdminBlogListEffect.CategoryDeleted -> messageBarState.addSuccess("دسته‌بندی حذف شد")
                is AdminBlogListEffect.CategorySaved -> messageBarState.addSuccess("دسته‌بندی ذخیره شد")
            }
        }
    }

    val onAdd = {
        if (selectedTab == 0) {
            navigateToManageBlog(null, null)
        } else {
            categoryToEdit = null
            showCategoryDialog = true
        }
    }

    Scaffold(
        floatingActionButton = {
            if (embedded) {
                FloatingActionButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        },
        topBar = topBarSlot@{
            if (embedded) return@topBarSlot
            TopAppBar(
                title = { Text("مدیریت مجله") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onAdd) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        ContentWithMessageBar(
            modifier = Modifier.fillMaxSize().padding(padding),
            messageBarState = messageBarState
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text(text = "مقاله‌ها", modifier = Modifier.padding(16.dp))
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text(text = "دسته‌بندی‌ها", modifier = Modifier.padding(16.dp))
                    }
                }

                if (state.isLoading) {
                    LoadingCard(modifier = Modifier.fillMaxSize())
                } else {
                    when (selectedTab) {
                        0 -> ArticlesList(
                            blogs = state.blogs,
                            navigateToManageBlog = navigateToManageBlog,
                            onDeleteBlog = { blog -> blogToDelete = blog }
                        )
                        1 -> CategoriesList(
                            categories = state.categories,
                            onEditCategory = { category ->
                                categoryToEdit = category
                                showCategoryDialog = true
                            },
                            onDeleteCategory = { category -> categoryToDelete = category }
                        )
                    }
                }
            }
        }
    }

    if (showCategoryDialog) {
        BlogCategoryDialog(
            category = categoryToEdit,
            onDismiss = { showCategoryDialog = false },
            onConfirm = { name, description ->
                val editing = categoryToEdit
                if (editing == null) {
                    viewModel.handleIntent(AdminBlogListIntent.CreateCategory(name, description))
                } else {
                    viewModel.handleIntent(AdminBlogListIntent.UpdateCategory(editing.id, name, description))
                }
                showCategoryDialog = false
            }
        )
    }

    blogToDelete?.let { blog ->
        AlertDialog(
            onDismissRequest = { blogToDelete = null },
            title = { Text("حذف مقاله") },
            text = { Text("آیا از حذف «${blog.title}» مطمئن هستید؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.handleIntent(AdminBlogListIntent.DeleteBlog(blog.id))
                    blogToDelete = null
                }) { Text("حذف", color = AppTheme.colors.sale, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { blogToDelete = null }) { Text("انصراف") } }
        )
    }

    categoryToDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("حذف دسته‌بندی") },
            text = { Text("آیا از حذف دسته‌بندی «${category.name}» مطمئن هستید؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.handleIntent(AdminBlogListIntent.DeleteCategory(category.id))
                    categoryToDelete = null
                }) { Text("حذف", color = AppTheme.colors.sale, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { categoryToDelete = null }) { Text("انصراف") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlogCategoryDialog(
    category: BlogCategory?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String?) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var description by remember { mutableStateOf(category?.description ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "دسته‌بندی جدید" else "ویرایش دسته‌بندی", fontFamily = AppFont()) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام دسته‌بندی", fontFamily = AppFont()) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("توضیحات (اختیاری)", fontFamily = AppFont()) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), description.trim().takeIf { it.isNotBlank() }) },
                enabled = name.trim().length >= 2
            ) { Text("ذخیره", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
private fun ArticlesList(
    blogs: List<Blog>,
    navigateToManageBlog: (Long?, String?) -> Unit,
    onDeleteBlog: (Blog) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(blogs) { blog ->
            val colors = AppTheme.colors
            val published = (blog.status ?: "PUBLISHED").uppercase() == "PUBLISHED"
            val statusColor = if (published) colors.ok else colors.star
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.line, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 54.dp, height = 40.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(colors.surfaceVariant)
                        .clickable { navigateToManageBlog(blog.id, blog.slug) }
                ) {
                    blog.thumbnailUrl?.let {
                        Image(
                            painter = rememberImagePainter(it),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.width(11.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = blog.title,
                        fontSize = FontSize.SMALL,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    blog.category?.let {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(text = it.name, fontSize = FontSize.EXTRA_SMALL, color = colors.primary)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (published) "منتشرشده" else "پیش‌نویس",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = FontSize.EXTRA_SMALL,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(colors.accentSoft)
                        .clickable { navigateToManageBlog(blog.id, blog.slug) },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp), tint = colors.primary) }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(colors.sale.copy(alpha = 0.1f))
                        .clickable { onDeleteBlog(blog) },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp), tint = colors.sale) }
            }
        }
    }
}

@Composable
private fun CategoriesList(
    categories: List<BlogCategory>,
    onEditCategory: (BlogCategory) -> Unit,
    onDeleteCategory: (BlogCategory) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { category ->
            val colors = AppTheme.colors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.line, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = category.name, fontSize = FontSize.REGULAR, fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(text = "${category.blogCount} مقاله", fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(colors.accentSoft)
                        .clickable { onEditCategory(category) },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp), tint = colors.primary) }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(colors.sale.copy(alpha = 0.1f))
                        .clickable { onDeleteCategory(category) },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp), tint = colors.sale) }
            }
        }
    }
}
