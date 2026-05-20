package com.kazemieh.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font


@Composable
fun AppFont() = FontFamily(
    Font(Res.font.typeface_fa)
)

object FontSize {
    val EXTRA_SMALL = 10.sp
    val SMALL = 12.sp
    val REGULAR = 14.sp
    val EXTRA_REGULAR = 16.sp
    val MEDIUM = 18.sp
    val EXTRA_MEDIUM = 20.sp
    val LARGE = 30.sp
    val EXTRA_LARGE = 40.sp
}