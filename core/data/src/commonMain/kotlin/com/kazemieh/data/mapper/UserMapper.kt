package com.kazemieh.data.mapper

import com.kazemieh.domain.model.User
import com.kazemieh.network.dto.response.UserResponse


fun UserResponse.toDomain(): User {
    return User(id, email)
}