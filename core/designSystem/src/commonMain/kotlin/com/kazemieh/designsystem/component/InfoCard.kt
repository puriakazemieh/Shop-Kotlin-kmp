package com.kazemieh.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.util.anyToString
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    image: DrawableResource,
    title: Any,
    subtitle: Any
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(60.dp),
            painter = painterResource(image),
            contentDescription = stringResource(Resources.String.InfoCardImageDesc)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = anyToString(title),
            fontSize = FontSize.MEDIUM,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = anyToString(subtitle),
            fontSize = FontSize.REGULAR,
            textAlign = TextAlign.Center
        )
    }
}
