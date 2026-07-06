package com.kazemieh.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.referral.GetMyReferralInfoUseCase
import com.kazemieh.domain.referral.ReferralInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReferralViewModel(
    private val getMyReferralInfoUseCase: GetMyReferralInfoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<AppResult<ReferralInfo>>(AppResult.Loading)
    val state: StateFlow<AppResult<ReferralInfo>> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.update { AppResult.Loading }
            _state.update { getMyReferralInfoUseCase() }
        }
    }
}
