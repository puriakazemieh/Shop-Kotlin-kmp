package com.kazemieh.clinic.resources

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Radius

private data class EmergencyResource(
    val title: String,
    val phone: String,
    val description: String
)

private val RESOURCES = listOf(
    EmergencyResource("اورژانسِ اجتماعی", "123", "برایِ خشونتِ خانگی، بحرانِ روانی و حمایتِ فوریِ اجتماعی — شبانه‌روزی و رایگان."),
    EmergencyResource("خطِ مشاوره‌ی سلامتِ روان (وزارتِ بهداشت)", "1480", "مشاوره‌ی تلفنیِ رایگان برایِ افکارِ خودکشی، بحرانِ روانی و اضطراب."),
    EmergencyResource("اورژانسِ پزشکی", "115", "برایِ خطرِ جانی یا نیازِ فوریِ پزشکی."),
    EmergencyResource("پلیس", "110", "در صورتِ خطرِ فوری برایِ خود یا دیگران.")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyResourcesScreen(navigateBack: () -> Unit) {
    val colors = AppTheme.colors
    val uriHandler = LocalUriHandler.current

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("منابعِ اورژانسی", fontSize = FontSize.MEDIUM, color = colors.onSurface) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().responsiveMaxWidth().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.md))
                        .background(colors.sale.copy(alpha = 0.1f))
                        .padding(14.dp)
                ) {
                    Text(
                        "اگر شما یا فردِ نزدیکِ شما در خطرِ فوری هستید، همین حالا با یکی از شماره‌های زیر تماس بگیرید.",
                        color = colors.sale, fontWeight = FontWeight.Bold, fontSize = FontSize.SMALL
                    )
                }
            }
            items(RESOURCES) { resource ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.md))
                        .background(colors.surface)
                        .clickable { uriHandler.openUri("tel:${resource.phone}") }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(resource.title, fontWeight = FontWeight.Bold, color = colors.onSurface, fontSize = FontSize.REGULAR)
                        Spacer(Modifier.height(4.dp))
                        Text(resource.description, color = colors.onSurfaceVariant, fontSize = FontSize.EXTRA_SMALL)
                    }
                    Spacer(Modifier.height(0.dp))
                    Text(
                        resource.phone,
                        color = colors.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = FontSize.LARGE
                    )
                }
            }
        }
    }
}
