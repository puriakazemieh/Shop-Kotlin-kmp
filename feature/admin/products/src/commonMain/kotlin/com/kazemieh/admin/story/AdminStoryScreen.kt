package com.kazemieh.admin.story

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.story.Story
import com.kazemieh.domain.story.StoryMediaType
import com.kazemieh.admin.products.PhotoPicker
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStoryScreen(
    navigateBack: () -> Unit
) {
    val viewModel = koinViewModel<AdminStoryViewModel>()
    val state by viewModel.state.collectAsState()
    val messageBarState = rememberMessageBarState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val mediaPicker = remember { PhotoPicker() }

    var selectedMediaBytes by remember { mutableStateOf<ByteArray?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    mediaPicker.InitializePhotoPicker(
        onImageSelect = { bytes ->
            selectedMediaBytes = bytes
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
            onDismiss = {
                showCreateDialog = false
                selectedMediaBytes = null
            },
            onConfirm = { title, productId ->
                viewModel.handleIntent(
                    AdminStoryIntent.CreateStory(
                        selectedMediaBytes!!,
                        StoryMediaType.IMAGE,
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
            TopAppBar(
                title = { Text(stringResource(Resources.String.AdminPanel) + " - Stories") },
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mediaPicker.open() }) {
                Icon(painterResource(Resources.Icon.Plus), contentDescription = null)
            }
        }
    ) { padding ->
        ContentWithMessageBar(
            modifier = Modifier.padding(padding),
            messageBarState = messageBarState
        ) {
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.stories) { story ->
                            AdminStoryCard(
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

@Composable
fun CreateStoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String?, productId: Long?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var productId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Story") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = productId,
                    onValueChange = { if (it.all { char -> char.isDigit() }) productId = it },
                    label = { Text("Product ID (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    title.ifBlank { null },
                    productId.toLongOrNull()
                )
            }) {
                Text("Upload")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminStoryCard(
    story: Story,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = story.mediaUrl,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = story.title ?: "No Title", style = MaterialTheme.typography.titleMedium)
                Text(text = story.createdAt, style = MaterialTheme.typography.bodySmall)
                story.productId?.let {
                    Text(
                        text = "Linked Product ID: $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(painterResource(Resources.Icon.Delete), contentDescription = null, tint = Color.Red)
            }
        }
    }
}
