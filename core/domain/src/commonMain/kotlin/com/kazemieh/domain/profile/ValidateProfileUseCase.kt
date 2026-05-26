package com.kazemieh.domain.profile

import com.kazemieh.domain.profile.Profile

class ValidateProfileUseCase {
    operator fun invoke(profile: Profile): Boolean {
        return profile.firstName?.length in 3..50 &&
                profile.lastName?.length in 3..50 &&
                (profile.city == null || profile.city.length in 3..50) &&
                (profile.postalCode == null || profile.postalCode.toString().length in 3..8) &&
                (profile.phone == null || profile.phone.length in 5..30)
    }
}
