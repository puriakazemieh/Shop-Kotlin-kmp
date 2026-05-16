package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.repository.AdminRepository

class UploadImageUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(bytes: ByteArray): AppResult<String> {
        return repository.uploadImage(bytes)
    }
}
