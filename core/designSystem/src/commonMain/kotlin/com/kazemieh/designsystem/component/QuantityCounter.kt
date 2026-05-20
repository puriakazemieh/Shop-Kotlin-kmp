package com.kazemieh.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource

enum class QuantityCounterSize { Small, Medium }

@Composable
fun QuantityCounter(
    modifier: Modifier = Modifier,
    size: QuantityCounterSize = QuantityCounterSize.Medium,
    value: Int,
    onMinusClick: (Int) -> Unit,
    onPlusClick: (Int) -> Unit
) {
    val iconSize = if (size == QuantityCounterSize.Small) 16.dp else 24.dp
    val padding = if (size == QuantityCounterSize.Small) 4.dp else 8.dp

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clickable { if (value > 1) onMinusClick(value - 1) }
                .padding(padding)
        ) {
            Icon(
                painter = painterResource(Resources.Icon.Minus),
                contentDescription = stringResource(Resources.String.MinusDesc),
                modifier = Modifier.size(iconSize)
            )
        }

        Text(
            text = value.toString(),
            fontSize = if (size == QuantityCounterSize.Small) FontSize.SMALL else FontSize.MEDIUM,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Box(
            modifier = Modifier
                .clickable { onPlusClick(value + 1) }
                .padding(padding)
        ) {
            Icon(
                painter = painterResource(Resources.Icon.Plus),
                contentDescription = stringResource(Resources.String.PlusDesc),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
