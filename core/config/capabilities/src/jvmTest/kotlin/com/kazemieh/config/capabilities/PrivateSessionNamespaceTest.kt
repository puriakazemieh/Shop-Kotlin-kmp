package com.kazemieh.config.capabilities

import kotlin.test.Test
import kotlin.test.assertNotEquals

class PrivateSessionNamespaceTest {
    @Test
    fun changesFingerprintWhenTenantBackendOrOriginChanges() {
        val base = PrivateSessionNamespace(BackendKind.WORDPRESS, "tenant-a", "https://shop.example.test/")

        assertNotEquals(base.fingerprint, PrivateSessionNamespace(BackendKind.WORDPRESS, "tenant-b", "https://shop.example.test/").fingerprint)
        assertNotEquals(base.fingerprint, PrivateSessionNamespace(BackendKind.SPRING, "tenant-a", "https://shop.example.test/").fingerprint)
        assertNotEquals(base.fingerprint, PrivateSessionNamespace(BackendKind.WORDPRESS, "tenant-a", "https://other.example.test/").fingerprint)
    }
}
