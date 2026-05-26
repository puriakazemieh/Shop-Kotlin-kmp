package com.kazemieh.auth

import com.kazemieh.auth.screen.AuthViewModel
import com.kazemieh.domain.auth.ForgotPasswordUseCase
import com.kazemieh.domain.auth.LoginUseCase
import com.kazemieh.domain.auth.RegisterUseCase
import com.kazemieh.domain.auth.ResetPasswordUseCase
import com.kazemieh.domain.auth.ValidateEmail
import com.kazemieh.domain.auth.ValidatePassword
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {

    // ViewModel
    viewModel {
        AuthViewModel(
            loginUseCase = get(),
            registerUseCase = get(),
            forgotPasswordUseCase = get(),
            resetPasswordUseCase = get(),
            validateEmail = get(),
            validatePassword = get()
        )
    }

    // UseCases
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { ForgotPasswordUseCase(get()) }
    factory { ResetPasswordUseCase(get()) }

    // Validation
    factory { ValidateEmail() }
    factory { ValidatePassword() }
}
