package com.kazemieh.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.Resources
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileForm(
    modifier: Modifier = Modifier,
    firstName: String?,
    onFirstNameChange: (String) -> Unit,
    lastName: String?,
    onLastNameChange: (String) -> Unit,
    email: String,
    phoneNumber: String?,
    onPhoneNumberChange: (String) -> Unit,
) {
    var showCountryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CustomTextField(
            value = firstName?:"",
            onValueChange = onFirstNameChange,
            placeholder = stringResource(Resources.String.FirstNamePlaceholder),
            error = firstName?.length !in 3..50
        )
        CustomTextField(
            value = lastName?:"",
            onValueChange = onLastNameChange,
            placeholder = stringResource(Resources.String.LastNamePlaceholder),
            error = lastName?.length !in 3..50
        )
        CustomTextField(
            value = email,
            onValueChange = {},
            placeholder = stringResource(Resources.String.EmailHint),
            enabled = false
        )
        CustomTextField(
            value = phoneNumber ?: "",
            onValueChange = onPhoneNumberChange,
            placeholder = stringResource(Resources.String.PhoneNumberPlaceholder),
            error = phoneNumber.toString().length !in 5..30,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )
    }
}