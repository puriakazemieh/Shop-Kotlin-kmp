//package com.kazemieh.designsystem.component
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import com.kazemieh.designsystem.FontSize
//import com.kazemieh.designsystem.Resources
//import com.nutrisport.shared.Constants.MAX_QUANTITY
//import com.nutrisport.shared.Constants.MIN_QUANTITY
//import com.nutrisport.shared.domain.QuantityCounterSize
//import org.jetbrains.compose.resources.painterResource
//
//@Composable
//fun QuantityCounter(
//    modifier: Modifier = Modifier,
//    size: QuantityCounterSize,
//    value: Int,
//    onMinusClick: (Int) -> Unit,
//    onPlusClick: (Int) -> Unit,
//) {
//    Row(
//        modifier = modifier,
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.spacedBy(size.spacing)
//    ) {
//        Box(
//            modifier = Modifier
//                .clip(RoundedCornerShape(size = 6.dp))
//                .background(MaterialTheme.colorScheme.primary)
//                .clickable { if (value > MIN_QUANTITY) onMinusClick(value - 1) }
//                .padding(size.padding),
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                modifier = Modifier.size(14.dp),
//                painter = painterResource(Resources.Icon.Minus),
//                contentDescription = "Minus icon",
//                tint = MaterialTheme.colorScheme.onPrimaryContainer
//            )
//        }
//        Box(
//            modifier = Modifier
//                .clip(RoundedCornerShape(size = 6.dp))
//                .background(MaterialTheme.colorScheme.primaryContainer)
//                .padding(size.padding),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = "+$value",
//                fontSize = FontSize.SMALL,
//                lineHeight = FontSize.SMALL * 1,
//                fontWeight = FontWeight.Medium,
//                color =  MaterialTheme.colorScheme.onPrimaryContainer
//            )
//        }
//        Box(
//            modifier = Modifier
//                .clip(RoundedCornerShape(size = 6.dp))
//                .background(MaterialTheme.colorScheme.primary)
//                .clickable { if (value < MAX_QUANTITY) onPlusClick(value + 1) }
//                .padding(size.padding),
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                modifier = Modifier.size(14.dp),
//                painter = painterResource(Resources.Icon.Plus),
//                contentDescription = "Plus icon",
//                tint = MaterialTheme.colorScheme.onPrimaryContainer
//            )
//        }
//    }
//}