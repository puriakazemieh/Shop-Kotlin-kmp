package com.kazemieh.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.membership.GetMembershipStatusUseCase
import com.kazemieh.domain.membership.MembershipStatus
import com.kazemieh.domain.membership.SubscribeMembershipUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface MembershipEffect {
    data class ShowError(val message: Any) : MembershipEffect
}

class MembershipViewModel(
    private val getMembershipStatusUseCase: GetMembershipStatusUseCase,
    private val subscribeMembershipUseCase: SubscribeMembershipUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<AppResult<MembershipStatus>>(AppResult.Loading)
    val state: StateFlow<AppResult<MembershipStatus>> = _state.asStateFlow()

    private val _isSubscribing = MutableStateFlow(false)
    val isSubscribing: StateFlow<Boolean> = _isSubscribing.asStateFlow()

    private val _effect = Channel<MembershipEffect>()
    val effect: Flow<MembershipEffect> = _effect.receiveAsFlow()

    fun load() {
        viewModelScope.launch {
            _state.update { AppResult.Loading }
            _state.update { getMembershipStatusUseCase() }
        }
    }

    fun subscribe() {
        viewModelScope.launch {
            _isSubscribing.update { true }
            when (val result = subscribeMembershipUseCase()) {
                is AppResult.Success -> _state.update { result }
                is AppResult.Error -> _effect.send(MembershipEffect.ShowError(result.message))
                else -> {}
            }
            _isSubscribing.update { false }
        }
    }
}
