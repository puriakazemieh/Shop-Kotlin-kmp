package com.kazemieh.designsystem.messagebar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MessageBarState {
    var success by mutableStateOf<Any?>(null)
        private set
    var errorMessage by mutableStateOf<Any?>(null)
        private set
    var errorException by mutableStateOf<Exception?>(null)
        private set
    internal var updated by mutableStateOf(false)
        private set

    fun addSuccess(message: Any) {
        clearMessages()
        success = message
        updated = !updated
    }

    fun addError(message: Any) {
        clearMessages()
        errorMessage = message
        updated = !updated
    }

    fun addError(exception: Exception) {
        clearMessages()
        errorException = exception
        updated = !updated
    }

    internal fun reset() {
        clearMessages()
        updated = !updated
    }

    private fun clearMessages() {
        success = null
        errorMessage = null
        errorException = null
    }
}
