package com.kazemieh.common

import kotlin.test.Test
import kotlin.test.assertEquals

class UserMessageTest {
    @Test
    fun `WordPress authentication errors have localized messages`() {
        assertEquals(Res.string.invalidCredentials, "BAD_CREDENTIALS".toUserMessage())
        assertEquals(Res.string.invalidOtp, "BAD_OTP".toUserMessage())
        assertEquals(Res.string.otpTemporarilyUnavailable, "OTP_UNAVAILABLE".toUserMessage())
        assertEquals(Res.string.tryAgainLater, "RATE_LIMIT".toUserMessage())
    }
}
