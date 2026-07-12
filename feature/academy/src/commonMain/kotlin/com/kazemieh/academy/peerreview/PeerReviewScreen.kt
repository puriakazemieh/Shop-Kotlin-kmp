package com.kazemieh.academy.peerreview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import com.kazemieh.domain.academy.ProjectSubmission
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

/** نقدِ همتایان: فهرستِ پروژه‌هایِ تاییدشده‌ی هم‌دوره‌ای‌ها + بحث/کامنتِ زیرِ هرکدام. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeerReviewScreen(
    courseId: Long,
    navigateBack: () -> Unit
) {
    val viewModel = koinViewModel<PeerReviewViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()

    LaunchedEffect(courseId) { viewModel.load(courseId) }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PeerReviewEffect.ShowError -> messageBarState.addError(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("نقدِ همتایان", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface, titleContentColor = colors.onSurface)
            )
        }
    ) { padding ->
        ContentWithMessageBar(modifier = Modifier.padding(padding), messageBarState = messageBarState) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = colors.primary) }
                state.submissions.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("هنوز پروژه‌ی تأییدشده‌ای برای نقد نیست.", color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(state.submissions, key = { it.id }) { submission ->
                        PeerSubmissionCard(
                            submission = submission,
                            expanded = state.expandedSubmissionId == submission.id,
                            comments = state.commentsBySubmission[submission.id].orEmpty(),
                            loadingComments = state.loadingComments && state.expandedSubmissionId == submission.id,
                            onToggle = { viewModel.toggleExpand(submission.id) },
                            onAddComment = { comment -> viewModel.addComment(submission.id, comment) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PeerSubmissionCard(
    submission: ProjectSubmission,
    expanded: Boolean,
    comments: List<com.kazemieh.domain.academy.PeerComment>,
    loadingComments: Boolean,
    onToggle: () -> Unit,
    onAddComment: (String) -> Unit
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .clickable { onToggle() }
            .padding(14.dp)
    ) {
        Text(submission.userName ?: "کاربر #${submission.userId}", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
        Spacer(Modifier.height(4.dp))
        Text("لینک: ${submission.fileUrl}", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
        if (!submission.note.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(submission.note, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
        }
        if (expanded) {
            Spacer(Modifier.height(10.dp))
            when {
                loadingComments -> Text("در حالِ بارگذاریِ نظرات…", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
                comments.isEmpty() -> Text("هنوز نظری ثبت نشده.", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
                else -> comments.forEach { c ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(c.userName, fontWeight = FontWeight.SemiBold, color = colors.onSurface, fontSize = FontSize.EXTRA_SMALL)
                        Text(c.comment, color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            var text by remember(submission.id) { mutableStateOf("") }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("نظرت را بنویس…", fontSize = FontSize.EXTRA_SMALL) },
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "ارسال",
                    modifier = Modifier
                        .clip(RoundedCornerShape(Radius.sm))
                        .background(if (text.isNotBlank()) colors.primary else colors.surfaceVariant)
                        .clickable(enabled = text.isNotBlank()) { onAddComment(text.trim()); text = "" }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    color = colors.onPrimary, fontSize = FontSize.SMALL, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
