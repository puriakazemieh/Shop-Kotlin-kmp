package com.kazemieh.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.ContentWidth
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.AppTheme
import com.kazemieh.designsystem.FontSize

/**
 * باتم‌شیتِ «ویرایش اطلاعات حساب» — مطابق اسپک کارمیلا.
 * برای ویرایشِ نام، نام خانوادگی و شماره‌ی موبایل استفاده می‌شود (ایمیل فقط‌خواندنی است).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditBottomSheet(
    initialFirstName: String,
    initialLastName: String,
    email: String,
    initialPhone: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (firstName: String, lastName: String, phone: String) -> Unit
) {
    val colors = AppTheme.colors
    var firstName by remember { mutableStateOf(initialFirstName) }
    var lastName by remember { mutableStateOf(initialLastName) }
    var phone by remember { mutableStateOf(initialPhone) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 32.dp, height = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .responsiveMaxWidth(ContentWidth.readable)
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "ویرایش اطلاعات حساب",
                fontFamily = AppFont(),
                fontSize = FontSize.LARGE,
                fontWeight = FontWeight.ExtraBold,
                color = colors.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            FieldLabel("نام")
            CustomTextField(
                value = firstName,
                onValueChange = { firstName = it },
                placeholder = "نام",
                error = firstName.length !in 3..50,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel("نام خانوادگی")
            CustomTextField(
                value = lastName,
                onValueChange = { lastName = it },
                placeholder = "نام خانوادگی",
                error = lastName.length !in 3..50,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel("ایمیل")
            CustomTextField(
                value = email,
                onValueChange = {},
                placeholder = "ایمیل",
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel("شماره موبایل")
            CustomTextField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = "شماره موبایل",
                error = phone.length !in 5..30,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("انصراف", fontFamily = AppFont(), color = colors.onSurface)
                }
                PrimaryButton(
                    text = if (isSaving) "در حال ذخیره…" else "ذخیره تغییرات",
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving &&
                        firstName.length in 3..50 &&
                        lastName.length in 3..50 &&
                        phone.length in 5..30,
                    onClick = { onConfirm(firstName, lastName, phone) }
                )
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontFamily = AppFont(),
        fontSize = FontSize.SMALL,
        fontWeight = FontWeight.SemiBold,
        color = AppTheme.colors.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}
