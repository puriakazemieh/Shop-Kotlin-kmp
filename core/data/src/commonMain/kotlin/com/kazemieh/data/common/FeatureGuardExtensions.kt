package com.kazemieh.data.common

import com.kazemieh.common.AppResult
import com.kazemieh.config.capabilities.FeatureUseCaseGuard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

inline fun <T> FeatureUseCaseGuard?.guardOrError(
    featureId: String,
    disabledMessage: String = "قابلیت '$featureId' در حال حاضر غیرفعال است.",
    block: () -> AppResult<T>
): AppResult<T> {
    if (this != null && !isEnabled(featureId)) {
        return AppResult.Error(disabledMessage, 403)
    }
    return block()
}

inline fun <T> FeatureUseCaseGuard?.guardFlow(
    featureId: String,
    disabledMessage: String = "قابلیت '$featureId' در حال حاضر غیرفعال است.",
    crossinline block: () -> Flow<AppResult<T>>
): Flow<AppResult<T>> = flow {
    if (this@guardFlow != null && !isEnabled(featureId)) {
        emit(AppResult.Error(disabledMessage, 403))
    } else {
        emitAll(block())
    }
}
