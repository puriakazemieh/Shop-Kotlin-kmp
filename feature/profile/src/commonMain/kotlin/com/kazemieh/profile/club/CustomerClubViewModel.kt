package com.kazemieh.profile.club

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.order.GetMyOrdersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CustomerClubViewModel(
    private val getMyOrdersUseCase: GetMyOrdersUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CustomerClubState())
    val state = _state.asStateFlow()

    init {
        loadPoints()
    }

    private fun loadPoints() {
        viewModelScope.launch {
            getMyOrdersUseCase().collectLatest { result ->
                when (result) {
                    is AppResult.Success -> {
                        val spent = result.data
                            .filter { !it.status.equals("CANCELLED", ignoreCase = true) }
                            .sumOf { it.totalPrice }
                        // هر ۱۰٬۰۰۰ تومان خرید = ۱ امتیاز باشگاه
                        val points = (spent / 10_000.0).toLong()
                        _state.update { it.copy(isLoading = false, points = points, tier = tierFor(points)) }
                    }
                    is AppResult.Error -> _state.update { it.copy(isLoading = false) }
                    is AppResult.Loading -> _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun tierFor(points: Long): ClubTier = when {
        points >= 15_000 -> ClubTier.PLATINUM
        points >= 5_000 -> ClubTier.GOLD
        points >= 1_000 -> ClubTier.SILVER
        else -> ClubTier.BRONZE
    }
}

enum class ClubTier(val title: String, val nextThreshold: Long?) {
    BRONZE("برنزی", 1_000),
    SILVER("نقره‌ای", 5_000),
    GOLD("طلایی", 15_000),
    PLATINUM("پلاتینی", null)
}

data class CustomerClubState(
    val isLoading: Boolean = true,
    val points: Long = 0,
    val tier: ClubTier = ClubTier.BRONZE
)
