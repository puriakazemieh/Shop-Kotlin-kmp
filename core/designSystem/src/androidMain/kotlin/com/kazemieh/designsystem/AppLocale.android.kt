package com.kazemieh.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

actual object AppLocale {
    @Composable
    actual infix fun provides(value: String): ProvidedValue<*> {
        val configuration = LocalConfiguration.current
        val locale = Locale.forLanguageTag(value)
        if (configuration.locales.isEmpty || (configuration.locales[0] != locale)) {
            Locale.setDefault(locale)
            configuration.setLocale(locale)
            
            val context = LocalContext.current
            val resources = context.resources
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        
        return LocalConfiguration.provides(configuration)
    }
}
