package com.kazemieh.network

interface TokenProvider {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
}
