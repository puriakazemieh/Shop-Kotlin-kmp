package com.kazemieh.clinic.list

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Mood
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius
import com.kazemieh.domain.clinic.TherapistSummary
import com.seiko.imageloader.rememberImagePainter
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapistListScreen(
    navigateBack: () -> Unit,
    navigateToTherapist: (String) -> Unit,
    navigateToMoodCheckIn: () -> Unit = {},
    navigateToEmergencyResources: () -> Unit = {}
) {
    val viewModel = koinViewModel<TherapistListViewModel>()
    val state by viewModel.state.collectAsState()
    val colors = AppTheme.colors

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("مشاوره و روان‌شناسی", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = navigateToMoodCheckIn) {
                        Icon(Icons.Default.Mood, contentDescription = "ثبتِ خلق‌وخو", tint = colors.onSurface)
                    }
                    IconButton(onClick = navigateToEmergencyResources) {
                        Icon(Icons.Default.Emergency, contentDescription = "منابعِ اورژانسی", tint = colors.sale)
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.specialtyQuery,
                onValueChange = viewModel::setSpecialtyQuery,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("جست‌وجو بر اساسِ تخصص…", fontSize = FontSize.SMALL, color = colors.onSurfaceVariant) },
                singleLine = true,
                shape = RoundedCornerShape(Radius.md),
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
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center), color = colors.primary
                    )
                    state.therapists.isEmpty() -> Text(
                        text = if (state.specialtyQuery.isBlank()) "درمانگری موجود نیست." else "درمانگری با این تخصص یافت نشد.",
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        color = colors.onSurfaceVariant
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(state.therapists) { therapist ->
                            TherapistRow(therapist = therapist, onClick = { navigateToTherapist(therapist.slug) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TherapistRow(therapist: TherapistSummary, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .background(colors.surface)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!therapist.photoUrl.isNullOrBlank()) {
                Image(
                    painter = rememberImagePainter(therapist.photoUrl!!),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("🧑‍⚕️", fontSize = FontSize.LARGE)
            }
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(therapist.name, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
            if (!therapist.specialty.isNullOrBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(therapist.specialty!!, color = colors.onSurfaceVariant, fontSize = FontSize.SMALL)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                if (therapist.availableSlotCount > 0) "${therapist.availableSlotCount} نوبتِ آزاد" else "نوبتِ آزادی نیست",
                color = if (therapist.availableSlotCount > 0) colors.ok else colors.onSurfaceVariant,
                fontSize = FontSize.SMALL,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
