package com.kazemieh.auth.component


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kazemieh.designsystem.AppTheme

@Composable
fun AuthButton(
    text: String,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}