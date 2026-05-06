package com.kazemieh.common

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object TokenExpiredEventBus {

    private val _events = MutableSharedFlow<AuthState>()
    val events: SharedFlow<AuthState> = _events.asSharedFlow()

    fun publish(event: AuthState) {
        _events.tryEmit(event)
    }

}