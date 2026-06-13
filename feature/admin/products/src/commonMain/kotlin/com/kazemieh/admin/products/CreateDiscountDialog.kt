package com.kazemieh.admin.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.CustomTextField
import com.kazemieh.domain.admin.DiscountType
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDiscountDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        code: String,
        type: DiscountType,
        value: Double,
        maxDiscountAmount: Double?,
        minOrderAmount: Double?,
        usageLimit: Int?,
        isActive: Boolean
    ) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(DiscountType.PERCENTAGE) }
    var value by remember { mutableStateOf("") }
    var maxDiscountAmount by remember { mutableStateOf("") }
    var minOrderAmount by remember { mutableStateOf("") }
    var usageLimit by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(Resources.String.CreateDiscount),
                    fontSize = FontSize.LARGE,
                    fontWeight = FontWeight.Bold
                )

                CustomTextField(
                    value = code,
                    onValueChange = { code = it },
                    placeholder = Resources.String.DiscountCodeLabel
                )

                Column {
                    Text(
                        text = stringResource(Resources.String.DiscountTypeLabel),
                        fontSize = FontSize.SMALL,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = type == DiscountType.PERCENTAGE,
                            onClick = { type = DiscountType.PERCENTAGE }
                        )
                        Text(stringResource(Resources.String.Percentage))
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = type == DiscountType.FIXED_AMOUNT,
                            onClick = { type = DiscountType.FIXED_AMOUNT }
                        )
                        Text(stringResource(Resources.String.FixedAmount))
                    }
                }

                CustomTextField(
                    value = value,
                    onValueChange = { value = it },
                    placeholder = Resources.String.DiscountValueLabel,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                if (type == DiscountType.PERCENTAGE) {
                    CustomTextField(
                        value = maxDiscountAmount,
                        onValueChange = { maxDiscountAmount = it },
                        placeholder = Resources.String.MaxDiscountAmountLabel,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                CustomTextField(
                    value = minOrderAmount,
                    onValueChange = { minOrderAmount = it },
                    placeholder = Resources.String.MinOrderAmountLabel,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                CustomTextField(
                    value = usageLimit,
                    onValueChange = { usageLimit = it },
                    placeholder = Resources.String.UsageLimitLabel,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                    Text(stringResource(Resources.String.IsActiveLabel))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(Resources.String.Cancel))
                    }
                    Button(
                        onClick = {
                            onConfirm(
                                code,
                                type,
                                value.toDoubleOrNull() ?: 0.0,
                                maxDiscountAmount.toDoubleOrNull(),
                                minOrderAmount.toDoubleOrNull(),
                                usageLimit.toIntOrNull(),
                                isActive
                            )
                        },
                        enabled = code.isNotBlank() && value.toDoubleOrNull() != null
                    ) {
                        Text(stringResource(Resources.String.Create))
                    }
                }
            }
        }
    }
}
