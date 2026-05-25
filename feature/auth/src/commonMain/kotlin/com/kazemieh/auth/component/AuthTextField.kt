package com.kazemieh.auth.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.kazemieh.designsystem.AppTheme

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    isPassword: Boolean = false
) {
    val colors = AppTheme.colors

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(hint) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.outline,
            focusedTextColor = colors.onSurface,
            unfocusedTextColor = colors.onSurface,
            cursorColor = colors.primary
        ),
        modifier = Modifier.fillMaxWidth()
    )
}