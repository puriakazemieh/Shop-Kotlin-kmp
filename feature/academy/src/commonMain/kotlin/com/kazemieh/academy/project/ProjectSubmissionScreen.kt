package com.kazemieh.academy.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel

/** ثبتِ پروژه‌ی پایانی برای دوره‌های پروژه‌محور — کاربر لینکِ پروژه (گیت‌هاب/درایو/...) را ثبت می‌کند. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectSubmissionScreen(
    courseId: Long,
    navigateBack: () -> Unit,
    navigateToPeerReview: (Long) -> Unit = {}
) {
    val viewModel = koinViewModel<ProjectSubmissionViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors
    val messageBarState = rememberMessageBarState()
    var fileUrl by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    LaunchedEffect(courseId) { viewModel.load(courseId) }
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ProjectSubmissionEffect.ShowError -> messageBarState.addError(effect.message)
                is ProjectSubmissionEffect.ShowSuccess -> messageBarState.addSuccess(effect.message)
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("پروژه‌ی پایانی", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
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
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = colors.primary) }
                return@ContentWithMessageBar
            }
            val submission = state.submission
            Column(modifier = Modifier.fillMaxSize().responsiveMaxWidth(com.kazemieh.designsystem.ContentWidth.readable).padding(16.dp)) {
                if (submission != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.md)).background(colors.surfaceVariant).padding(14.dp)
                    ) {
                        val statusLabel = when (submission.status) {
                            "APPROVED" -> "تأییدشده ✓"
                            "REJECTED" -> "ردشده — لطفاً اصلاح و دوباره ثبت کنید"
                            else -> "در انتظارِ بررسی"
                        }
                        val statusColor = when (submission.status) {
                            "APPROVED" -> colors.ok
                            "REJECTED" -> colors.sale
                            else -> colors.onSurfaceVariant
                        }
                        Text("وضعیتِ پروژه‌ی ثبت‌شده", fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.SMALL)
                        Spacer(Modifier.height(4.dp))
                        Text(statusLabel, color = statusColor, fontSize = FontSize.SMALL, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text("لینک: ${submission.fileUrl}", color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
                        if (!submission.mentorFeedback.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text("بازخوردِ مدرس:", fontWeight = FontWeight.SemiBold, color = colors.onSurface, fontSize = FontSize.EXTRA_SMALL)
                            Text(submission.mentorFeedback!!, color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (submission.status == "REJECTED") "ثبتِ نسخه‌ی جدید" else "ثبتِ دوباره (جایگزینِ نسخه‌ی قبلی)",
                        fontWeight = FontWeight.SemiBold, color = colors.onSurface, fontSize = FontSize.SMALL
                    )
                    Spacer(Modifier.height(8.dp))
                }
                ProjectTextField(value = fileUrl, onValueChange = { fileUrl = it }, label = "لینکِ پروژه (گیت‌هاب/درایو/...)")
                Spacer(Modifier.height(8.dp))
                ProjectTextField(value = note, onValueChange = { note = it }, label = "توضیحِ تکمیلی (اختیاری)")
                Spacer(Modifier.height(12.dp))
                val enabled = fileUrl.isNotBlank() && !state.submitting
                Text(
                    if (state.submitting) "در حالِ ارسال…" else "ثبتِ پروژه",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.button))
                        .background(if (enabled) colors.primary else colors.line)
                        .clickable(enabled = enabled) {
                            viewModel.submit(fileUrl.trim(), note.trim().ifBlank { null })
                            fileUrl = ""; note = ""
                        }
                        .padding(vertical = 14.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = FontSize.REGULAR
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "نقدِ همتایان — دیدنِ پروژه‌های تأییدشده‌ی هم‌دوره‌ای‌ها",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.button))
                        .background(colors.surfaceVariant)
                        .clickable { navigateToPeerReview(courseId) }
                        .padding(vertical = 14.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = colors.onSurface, fontWeight = FontWeight.SemiBold, fontSize = FontSize.SMALL
                )
            }
        }
    }
}

@Composable
private fun ProjectTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    val colors = AppTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, fontSize = FontSize.EXTRA_SMALL, color = colors.onSurfaceVariant) },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.line,
            cursorColor = colors.primary,
            focusedTextColor = colors.onSurface,
            unfocusedTextColor = colors.onSurface
        )
    )
}
