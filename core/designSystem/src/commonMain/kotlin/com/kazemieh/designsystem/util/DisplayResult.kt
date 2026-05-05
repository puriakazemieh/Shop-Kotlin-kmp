package com.kazemieh.designsystem.util

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kazemieh.common.AppResult


@Composable
fun <T> AppResult<T>.DisplayResult(
    modifier: Modifier = Modifier,
    onLoading: (@Composable () -> Unit)? = null,
    onError: (@Composable (String) -> Unit)? = null,
    onSuccess: @Composable (T) -> Unit,
    transitionSpec: ContentTransform? = scaleIn(tween(durationMillis = 400))
            + fadeIn(tween(durationMillis = 800))
            togetherWith scaleOut(tween(durationMillis = 400))
            + fadeOut(
        tween(durationMillis = 800)
    ),
    backgroundColor: Color? = null
) {
    AnimatedContent(
        modifier = modifier
            .background(color = backgroundColor ?: Color.Unspecified),
        targetState = this,
        transitionSpec = {
            transitionSpec ?: (EnterTransition.None togetherWith ExitTransition.None)
        },
        label = "Content Animation"
    ) { state ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            when (state) {
                is AppResult.Loading -> {
                    onLoading?.invoke()
                }

                is AppResult.Error -> {
                    onError?.invoke(state.message)
                }

                is AppResult.Success -> {
                    onSuccess(state.data)
                }
            }
        }
    }
}