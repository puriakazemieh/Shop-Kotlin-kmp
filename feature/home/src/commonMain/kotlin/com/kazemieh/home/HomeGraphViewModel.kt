package com.kazemieh.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.common.CartEventBus
import com.kazemieh.domain.usecase.IsUserLoggedInUseCase
import com.kazemieh.domain.usecase.ObserveAuthStateUseCase
import com.kazemieh.domain.usecase.ObserveProfileUseCase
import com.kazemieh.domain.usecase.SignOutUseCase
import com.kazemieh.domain.usecase.cart.GetCartUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeGraphViewModel(
    observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val observeProfileUseCase: ObserveProfileUseCase,
    private val getCartUseCase: GetCartUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        handleIntent(HomeIntent.RefreshAuthState)
        observeProfile()
        observeCart()
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.SignOut -> signOut()
            is HomeIntent.RefreshAuthState -> checkAuthState()
        }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            observeProfileUseCase().collect { result ->
                if (result is AppResult.Success) {
                    _state.update { it.copy(isAdmin = result.data.role == "ADMIN") }
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
    val isAdmin: Boolean = false,
    val cartItemCount: Int = 0,
    val error: String? = null
)

sealed interface HomeEffect {
    data object NavigateToAuth : HomeEffect
    data class ShowError(val message: String) : HomeEffect
}