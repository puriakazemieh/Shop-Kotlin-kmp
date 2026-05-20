package com.kazemieh.common

enum class AppThemeMode(val code: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");

    companion object {
        fun fromCode(code: String): AppThemeMode {
            return entries.find { it.code == code } ?: SYSTEM
        }
    }
}
