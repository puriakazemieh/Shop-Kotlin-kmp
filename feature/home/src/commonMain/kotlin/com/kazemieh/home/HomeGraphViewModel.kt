package com.kazemieh.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.usecase.IsUserLoggedInUseCase
import com.kazemieh.domain.usecase.ObserveAuthStateUseCase
import com.kazemieh.domain.usecase.SignOutUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeGraphViewModel(
    observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        handleIntent(HomeIntent.RefreshAuthState)
//        viewModelScope.launch {
//            observeAuthStateUseCase().collect { authState ->
//                if (authState is AuthState.Unauthenticated) {
//                    _effect.send(HomeEffect.NavigateToAuth)
//                }
//            }
//        }
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.SignOut -> signOut()
            is HomeIntent.RefreshAuthState -> checkAuthState()
        }
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            isUserLoggedInUseCase().collect { isLoggedIn ->
                _state.update { it.copy(isLoggedIn = isLoggedIn) }
            }
        }
    }


    private fun signOut() {
        viewModelScope.launch {
            when (val result = signOutUseCase()) {
                is AppResult.Success -> {
                    _state.update { it.copy(isLoggedIn = false) }
//                    _effect.send(HomeEffect.NavigateToAuth)
                }

                is AppResult.Error -> {
                    _effect.send(HomeEffect.ShowError(result.message))
                }

                else -> {}
            }
        }
    }
}


sealed interface HomeIntent {
    data object SignOut : HomeIntent
    data object RefreshAuthState : HomeIntent
}

data class HomeState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

sealed interface HomeEffect {
    data object NavigateToAuth : HomeEffect
    data class ShowError(val message: String) : HomeEffect
}