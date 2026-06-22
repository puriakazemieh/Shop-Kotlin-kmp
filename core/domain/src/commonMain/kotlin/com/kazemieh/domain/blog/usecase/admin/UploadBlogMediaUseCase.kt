package com.kazemieh.domain.blog.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.blog.BlogRepository

class UploadBlogMediaUseCase(
    private val repository: BlogRepository
) {
    suspend operator fun invoke(fileBytes: ByteArray, fileName: String): AppResult<String> {
        return repository.uploadMedia(fileBytes, fileName)
    }
}
