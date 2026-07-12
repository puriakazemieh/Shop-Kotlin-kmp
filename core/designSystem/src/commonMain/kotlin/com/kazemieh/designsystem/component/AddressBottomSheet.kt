package com.kazemieh.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.unit.dp
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.ContentWidth
import com.kazemieh.designsystem.responsiveMaxWidth
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressBottomSheet(
    initialReceiverName: String = "",
    initialReceiverPhone: String = "",
    initialProvince: String = "",
    initialCity: String = "",
    initialAddressLine1: String = "",
    initialAddressLine2: String? = null,
    initialPostalCode: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        receiverName: String,
        receiverPhone: String,
        province: String,
        city: String,
        addressLine1: String,
        addressLine2: String?,
        postalCode: String?
    ) -> Unit
) {
    var receiverName by remember { mutableStateOf(initialReceiverName) }
    var receiverPhone by remember { mutableStateOf(initialReceiverPhone) }
    var province by remember { mutableStateOf(initialProvince) }
    var city by remember { mutableStateOf(initialCity) }
    var addressLine1 by remember { mutableStateOf(initialAddressLine1) }
    var addressLine2 by remember { mutableStateOf(initialAddressLine2 ?: "") }
    var postalCode by remember { mutableStateOf(initialPostalCode ?: "") }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Resources.String.AddNewAddress),
                fontFamily = AppFont(),
                fontSize = FontSize.LARGE,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            CustomTextField(
                value = receiverName,
                onValueChange = { receiverName = it },
                placeholder = stringResource(Resources.String.ReceiverNamePlaceholder),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                value = receiverPhone,
                onValueChange = { receiverPhone = it },
                placeholder = stringResource(Resources.String.ReceiverPhonePlaceholder),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                value = province,
                onValueChange = { province = it },
                placeholder = stringResource(Resources.String.ProvincePlaceholder),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                value = city,
                onValueChange = { city = it },
                placeholder = stringResource(Resources.String.CityPlaceholder),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                value = addressLine1,
                onValueChange = { addressLine1 = it },
                placeholder = stringResource(Resources.String.AddressLine1Placeholder),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                value = addressLine2,
                onValueChange = { addressLine2 = it },
                placeholder = stringResource(Resources.String.AddressLine2OptionalPlaceholder),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                value = postalCode,
                onValueChange = { postalCode = it },
                placeholder = stringResource(Resources.String.PostalCodeOptionalPlaceholder),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = stringResource(Resources.String.SaveAddress),
                enabled = receiverName.isNotBlank() && receiverPhone.isNotBlank() && province.isNotBlank() && city.isNotBlank() && addressLine1.isNotBlank(),
                onClick = {
                    onConfirm(
                        receiverName,
                        receiverPhone,
                        province,
                        city,
                        addressLine1,
                        addressLine2.takeIf { it.isNotBlank() },
                        postalCode.takeIf { it.isNotBlank() }
                    )
                }
            )
        }
    }
}
