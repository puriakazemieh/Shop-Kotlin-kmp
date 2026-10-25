package com.kazemieh.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UserMessageTest {
    @Test
    fun `WordPress authentication errors have localized messages`() {
        assertEquals(Res.string.invalidCredentials, "BAD_CREDENTIALS".toUserMessage())
        assertEquals(Res.string.invalidOtp, "BAD_OTP".toUserMessage())
        assertEquals(Res.string.otpTemporarilyUnavailable, "OTP_UNAVAILABLE".toUserMessage())
        assertEquals(Res.string.tryAgainLater, "RATE_LIMIT".toUserMessage())
    }

    @Test
    fun `active Bridge error codes never fall back to the unknown message`() {
        val bridgeCodes = listOf(
            "BAD_CREDENTIALS", "BAD_OTP", "EMAIL_ALREADY_EXISTS", "FEATURE_DISABLED",
            "INVALID_COUPON", "INVALID_IMAGE", "INVALID_INPUT", "INVALID_SLOT",
            "INVALID_STATUS", "INVALID_URL", "INVALID_VIDEO", "INVALID_WINDOW",
            "NO_FILE", "NO_FREE_SEAT", "NO_QUIZ", "NOT_CANCELLABLE", "NOT_FOUND",
            "ORDER_FAILED", "SLOT_TAKEN", "STALE_VERSION", "UNAVAILABLE", "UPDATE_FAILED",
            "UPLOAD_FAILED", "VARIANT_REQUIRED", "WALLET_DISABLED", "WOO_INACTIVE",
            "WOOCOMMERCE_INACTIVE", "WEAK_PASSWORD", "RATE_LIMIT", "UNAUTHORIZED"
        )
        bridgeCodes.forEach { code ->
            assertNotEquals(Res.string.unknownError, code.toUserMessage(), code)
        }
    }
}
