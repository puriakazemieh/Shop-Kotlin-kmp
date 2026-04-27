package com.kazemieh.home.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
//    customer: RequestState<Customer>,
    selected: BottomBarDestination,
    onSelect: (BottomBarDestination) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(size = 12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(
                vertical = 24.dp,
                horizontal = 36.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BottomBarDestination.entries.forEach { destination ->
            val animatedTint by animateColorAsState(
                targetValue = if (selected == destination) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onPrimary,
            )
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    modifier = Modifier.clickable { onSelect(destination) },
                    painter = painterResource(destination.icon),
                    contentDescription = "Bottom Bar destination icon",
                    tint = animatedTint
                )
                if (destination == BottomBarDestination.Cart) {
//                    AnimatedContent(
//                        targetState = customer
//                    ) { customerState ->
//                        if (customerState.isSuccess() && customerState.getSuccessData().cart.isNotEmpty()) {
//                            Box(
//                                modifier = Modifier
//                                    .size(8.dp)
//                                    .offset(x = 4.dp, y = (-4).dp)
//                                    .clip(CircleShape)
//                                    .background(IconSecondary)
//                            )
//                        }
//                    }
                }
            }
        }
    }
}