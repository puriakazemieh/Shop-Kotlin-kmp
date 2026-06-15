package com.kazemieh.catalog

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kazemieh.domain.story.Story
import com.kazemieh.domain.story.StoryMediaType
import kotlinx.coroutines.launch

@Composable
fun StoryDetailScreen(
    stories: List<Story>,
    initialIndex: Int,
    onClose: () -> Unit,
    onStorySeen: (Long) -> Unit,
    onProductClick: (Long) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { stories.size }
    val coroutineScope = rememberCoroutineScope()
    var isGlobalPaused by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = !isGlobalPaused
        ) { page ->
            StoryItemDisplay(
                story = stories[page],
                isActive = pagerState.currentPage == page,
                isMuted = isMuted,
                onNext = {
                    if (page < stories.size - 1) {
                        coroutineScope.launch { pagerState.animateScrollToPage(page + 1) }
                    } else {
                        onClose()
                    }
                },
                onPrevious = {
                    if (page > 0) {
                        coroutineScope.launch { pagerState.animateScrollToPage(page - 1) }
                    }
                },
                onStorySeen = { onStorySeen(stories[page].id) },
                onProductClick = onProductClick,
                onPauseStateChange = { isGlobalPaused = it }
            )
        }

        // Top Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
        ) {
            // Progress Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                stories.forEachIndexed { index, _ ->
                    StoryProgressBar(
                        modifier = Modifier.weight(1f),
                        isActive = index == pagerState.currentPage,
                        isCompleted = index < pagerState.currentPage,
                        isPaused = isGlobalPaused
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isMuted = !isMuted }) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Mute/Unmute",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun StoryItemDisplay(
    story: Story,
    isActive: Boolean,
    isMuted: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onStorySeen: () -> Unit,
    onProductClick: (Long) -> Unit,
    onPauseStateChange: (Boolean) -> Unit
) {
    var isPausedByInteraction by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    
    val isEffectivelyPaused = isPausedByInteraction || isBuffering

    LaunchedEffect(isEffectivelyPaused) {
        onPauseStateChange(isEffectivelyPaused)
    }

    LaunchedEffect(isActive) {
        if (isActive) {
            onStorySeen()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { isPausedByInteraction = true },
                    onPress = {
                        try {
                            awaitRelease()
                        } finally {
                            isPausedByInteraction = false
                        }
                    },
                    onTap = { offset ->
                        if (offset.x < size.width / 3) {
                            onPrevious()
                        } else {
                            onNext()
                        }
                    }
                )
            }
    ) {
        if (story.mediaType == StoryMediaType.IMAGE) {
            AsyncImage(
                model = story.mediaUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Timer for Image
            if (isActive && !isEffectivelyPaused) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(5000)
                    onNext()
                }
            }
        } else {
            VideoPlayer(
                url = story.mediaUrl,
                isPaused = isPausedByInteraction || !isActive,
                isMuted = isMuted,
                onLoading = { isBuffering = it },
                onEnd = onNext
            )
        }

        if (isBuffering && isActive) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        // Title and Product Button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            story.title?.let {
                Text(
                    text = it,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }

            story.productId?.let { id ->
                Button(
                    onClick = { onProductClick(id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.8f))
                ) {
                    Text("مشاهده محصول", color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun StoryProgressBar(
    modifier: Modifier,
    isActive: Boolean,
    isCompleted: Boolean,
    isPaused: Boolean
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(isActive, isCompleted, isPaused) {
        if (isActive) {
            if (isPaused) {
                progress.stop()
            } else {
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = (5000 * (1f - progress.value)).toInt(),
                        easing = LinearEasing
                    )
                )
            }
        } else {
            progress.snapTo(if (isCompleted) 1f else 0f)
        }
    }

    LinearProgressIndicator(
        progress = { progress.value },
        modifier = modifier
            .height(2.dp)
            .clip(RoundedCornerShape(1.dp)),
        color = Color.White,
        trackColor = Color.White.copy(alpha = 0.3f)
    )
}
