package com.kazemieh.data.local

import com.kazemieh.domain.profile.Profile
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProfileLocalDataSource(
    private val settings: Settings
) {
    private val _profileFlow = MutableStateFlow<Profile?>(null)

    companion object {
        private const val KEY_ID = "profile_id"
        private const val KEY_EMAIL = "profile_email"
        private const val KEY_FIRST_NAME = "profile_first_name"
        private const val KEY_LAST_NAME = "profile_last_name"
        private const val KEY_PHONE = "profile_phone"
        private const val KEY_CITY = "profile_city"
        private const val KEY_ROLE = "profile_role"
        private const val KEY_POSTAL_CODE = "profile_postal_code"
        private const val KEY_ADDRESS = "profile_address"
    }

    init {
        _profileFlow.value = loadProfile()
    }

    private fun loadProfile(): Profile? {
        val id = settings.getLongOrNull(KEY_ID) ?: return null
        return Profile(
            id = id,
            email = settings.getString(KEY_EMAIL, ""),
            firstName = settings.getString(KEY_FIRST_NAME, ""),
            lastName = settings.getString(KEY_LAST_NAME, ""),
            phone = settings.getStringOrNull(KEY_PHONE),
            city = settings.getStringOrNull(KEY_CITY),
            role = settings.getString(KEY_ROLE, ""),
            postalCode = settings.getIntOrNull(KEY_POSTAL_CODE),
        )
    }

    suspend fun saveProfile(profile: Profile) {
        settings.putLong(KEY_ID, profile.id)
        settings.putString(KEY_EMAIL, profile.email)
        settings.putString(KEY_ROLE, profile.role)

        if (profile.firstName != null) {
            settings.putString(KEY_FIRST_NAME, profile.firstName!!)
        } else {
            settings.remove(KEY_FIRST_NAME)
        }

        if (profile.lastName != null) {
            settings.putString(KEY_LAST_NAME, profile.lastName!!)
        } else {
            settings.remove(KEY_LAST_NAME)
        }

        if (profile.phone != null) {
            settings.putString(KEY_PHONE, profile.phone!!)
        } else {
            settings.remove(KEY_PHONE)
        }

        if (profile.city != null) {
            settings.putString(KEY_CITY, profile.city!!)
        } else {
            settings.remove(KEY_CITY)
        }

        if (profile.postalCode != null) {
            settings.putInt(KEY_POSTAL_CODE, profile.postalCode!!)
        } else {
            settings.remove(KEY_POSTAL_CODE)
        }

        _profileFlow.update { profile }
    }

    fun observeProfile(): Flow<Profile?> {
        return _profileFlow.asStateFlow()
    }

    suspend fun clearProfile() {
        settings.clear()
        _profileFlow.update { null }
    }
}
