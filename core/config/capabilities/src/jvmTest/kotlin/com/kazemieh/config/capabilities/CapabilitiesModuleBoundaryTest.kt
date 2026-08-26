package com.kazemieh.config.capabilities

import kotlin.test.Test
import kotlin.test.assertEquals

class CapabilitiesModuleBoundaryTest {
    @Test
    fun hasItsOwnStableModuleId() {
        assertEquals("core:config:capabilities", CapabilitiesModule.moduleId)
    }
}
