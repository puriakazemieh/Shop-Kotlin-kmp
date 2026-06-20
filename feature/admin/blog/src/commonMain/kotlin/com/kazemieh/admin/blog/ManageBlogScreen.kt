package com.kazemieh.admin.blog

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.component.CustomTextField
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.domain.blog.Blog
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

    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var thumbnailUrl by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("PUBLISHED") }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var isFeatured by remember { mutableStateOf(false) }
    var metaTitle by remember { mutableStateOf("") }
    var metaDescription by remember { mutableStateOf("") }

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
                title = { Text(if (id == null) "Create Blog" else "Edit Blog") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val blog = Blog(
                            id = id ?: 0L,
                            title = title,
                            slug = slug ?: "",
                            summary = summary,
                            thumbnailUrl = thumbnailUrl,
                            status = status,
                            category = state.categories.find { it.id == categoryId },
                            isFeatured = isFeatured,
                            metaTitle = metaTitle,
                            metaDescription = metaDescription,
                            viewCount = state.blog?.viewCount ?: 0,
                            readingTimeMinutes = state.blog?.readingTimeMinutes ?: 5,
                            createdAt = state.blog?.createdAt ?: ""
                        )
                        viewModel.saveBlog(blog)
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
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
                CustomTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Title",
                    modifier = Modifier.fillMaxWidth()
                )
                CustomTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    placeholder = "Summary",
                    modifier = Modifier.fillMaxWidth()
                )
                CustomTextField(
                    value = thumbnailUrl,
                    onValueChange = { thumbnailUrl = it },
                    placeholder = "Thumbnail URL",
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (thumbnailUrl.isNotEmpty()) {
                    Image(
                        painter = rememberImagePainter(thumbnailUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(modifier = Modifier.weight(1f)) {
                        Checkbox(checked = isFeatured, onCheckedChange = { isFeatured = it })
                        Text("Featured", modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Text("Status")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = status == "PUBLISHED",
                        onClick = { status = "PUBLISHED" },
                        label = { Text("Published") }
                    )
                    FilterChip(
                        selected = status == "DRAFT",
                        onClick = { status = "DRAFT" },
                        label = { Text("Draft") }
                    )
                }

                CustomTextField(
                    value = metaTitle,
                    onValueChange = { metaTitle = it },
                    placeholder = "Meta Title",
                    modifier = Modifier.fillMaxWidth()
                )
                CustomTextField(
                    value = metaDescription,
                    onValueChange = { metaDescription = it },
                    placeholder = "Meta Description",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
