package com.kazemieh.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.common.CartEventBus
import com.kazemieh.domain.profile.GetProfileUseCase
import com.kazemieh.domain.auth.ObserveAuthStateUseCase
import com.kazemieh.domain.profile.ObserveProfileUseCase
import com.kazemieh.domain.auth.SignOutUseCase
import com.kazemieh.domain.cart.GetCartUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val observeProfileUseCase: ObserveProfileUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val getCartUseCase: GetCartUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    private val _effect = Channel<MainEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        observeAuthState()
        observeProfile()
        observeCart()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            observeAuthStateUseCase().collect { authState ->
                val isLoggedIn = authState is com.kazemieh.common.AuthState.Authenticated
                _state.update { it.copy(isLoggedIn = isLoggedIn) }
                if (isLoggedIn) {
                    getProfileUseCase()
                } else {
                    _state.update { it.copy(isAdmin = false) }
                }
            }
        }
    }

    fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.SignOut -> signOut()
        }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            observeProfileUseCase().collect { result ->
                if (result is AppResult.Success) {
                    val p = result.data
                    _state.update {
                        it.copy(
                            isAdmin = p.role == "ADMIN",
                            userName = "${p.firstName.orEmpty()} ${p.lastName.orEmpty()}".trim(),
                            userPhone = p.phone ?: p.mobile ?: ""
                        )
                    }
                }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeCart() {
        viewModelScope.launch {
            merge(flowOf(Unit), CartEventBus.events)
                .flatMapLatest { getCartUseCase() }
                .collect { result ->
                    if (result is AppResult.Success) {
                        _state.update { it.copy(cartItemCount = result.data.totalQty) }
                    }
                }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            when (val result = signOutUseCase()) {
                is AppResult.Success -> {
                    _state.update { it.copy(isLoggedIn = false, isAdmin = false) }
                    _effect.send(MainEffect.NavigateToAuth)
                }

                is AppResult.Error -> {
                    _effect.send(MainEffect.ShowError(result.message))
                }

                else -> {}
            }
        }
    }
}


sealed interface MainIntent {
    data object SignOut : MainIntent
}

data class MainState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isAdmin: Boolean = false,
    val userName: String = "",
    val userPhone: String = "",
    val cartItemCount: Int = 0,
    val error: Any? = null
)

sealed interface MainEffect {
    data object NavigateToAuth : MainEffect
    data class ShowError(val message: Any) : MainEffect
}
