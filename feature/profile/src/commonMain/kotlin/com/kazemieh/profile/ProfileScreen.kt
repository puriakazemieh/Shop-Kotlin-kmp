package com.kazemieh.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazemieh.common.AppResult
import com.kazemieh.designsystem.AppFont
import com.kazemieh.designsystem.FontSize
import com.kazemieh.designsystem.Resources
import com.kazemieh.designsystem.component.InfoCard
import com.kazemieh.designsystem.component.LoadingCard
import com.kazemieh.designsystem.component.PrimaryButton
import com.kazemieh.designsystem.component.ProfileForm
import com.kazemieh.designsystem.messagebar.ContentWithMessageBar
import com.kazemieh.designsystem.messagebar.rememberMessageBarState
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navigateBack: () -> Unit,
) {
    val viewModel = koinViewModel<ProfileViewModel>()
    val state by viewModel.state.collectAsState()
    val messageBarState = rememberMessageBarState()

    LaunchedEffect(Unit) {
        viewModel.event.collect { effect ->
            when (effect) {
                is UiEvent.ShowSuccess -> {
                    messageBarState.addSuccess(effect.message)
                }

                is UiEvent.ShowError -> {
                    messageBarState.addError(effect.message)
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.onSecondary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile",
                        fontFamily = AppFont(),
                        fontSize = FontSize.LARGE,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            painter = painterResource(Resources.Icon.BackArrow),
                            contentDescription = "Back Arrow icon",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onSecondary,
                    scrolledContainerColor = MaterialTheme.colorScheme.onSecondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        ContentWithMessageBar(
            modifier = Modifier.padding(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding()
            ),
            messageBarState = messageBarState,
            errorMaxLines = 2,
            errorContainerColor = MaterialTheme.colorScheme.error,
            errorContentColor = MaterialTheme.colorScheme.primaryContainer,
            successContainerColor = MaterialTheme.colorScheme.primary,
            successContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp, bottom = 24.dp)
                    .imePadding()
            ) {
                when (val displayState = state.displayState) {
                    is AppResult.Loading -> {
                        LoadingCard(modifier = Modifier.fillMaxSize())
                    }

                    is AppResult.Error -> {
                        InfoCard(
                            modifier = Modifier.fillMaxSize(),
                            image = Resources.Image.Cat,
                            title = "Oops!",
                            subtitle = displayState.message
                        )
                    }

                    is AppResult.Success -> {
                        state.profile?.let { profile ->
                            Column(modifier = Modifier.fillMaxSize()) {
                                ProfileForm(
                                    modifier = Modifier.weight(1f),
                                    firstName = profile.firstName,
                                    onFirstNameChange = { value ->
                                        viewModel.handleIntent(
                                            ProfileIntent.UpdateFirstName(value)
                                        )
                                    },
                                    lastName = profile.lastName,
                                    onLastNameChange = { value ->
                                        viewModel.handleIntent(
                                            ProfileIntent.UpdateLastName(value)
                                        )
                                    },
                                    email = profile.email,
                                    city = profile.city,
                                    onCityChange = { value ->
                                        viewModel.handleIntent(
                                            ProfileIntent.UpdateCity(value)
                                        )
                                    },
                                    postalCode = profile.postalCode,
                                    onPostalCodeChange = { value ->
                                        viewModel.handleIntent(
                                            ProfileIntent.UpdatePostalCode(value)
                                        )
                                    },
                                    address = null,
                                    onAddressChange = { value ->
                                        viewModel.handleIntent(
                                            ProfileIntent.UpdateAddress(value)
                                        )
                                    },
                                    phoneNumber = profile.phone,
                                    onPhoneNumberChange = { value ->
                                        viewModel.handleIntent(
                                            ProfileIntent.UpdatePhoneNumber(value)
                                        )
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                PrimaryButton(
                                    text = if (state.isSaving) "Saving..." else "Update",
                                    icon = Resources.Icon.Checkmark,
                                    enabled = state.isFormValid && !state.isSaving,
                                    onClick = {
                                        viewModel.handleIntent(ProfileIntent.SaveProfile)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
