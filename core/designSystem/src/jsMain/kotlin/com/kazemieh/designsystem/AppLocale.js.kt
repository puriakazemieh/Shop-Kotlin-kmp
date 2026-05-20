package com.kazemieh.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalJsLocale = staticCompositionLocalOf { "en" }

actual object AppLocale {
    @Composable
    actual infix fun provides(value: String): ProvidedValue<*> {
        val lang = value.replace("_", "-")
        js(
            """
            if (!window.__custom_languages_intercepted) {
                window.__custom_languages_intercepted = true;
                var originalLanguagesDescriptor = Object.getOwnPropertyDescriptor(Navigator.prototype, 'languages');
                Object.defineProperty(Navigator.prototype, 'languages', {
                    get: function() {
                        return window.__custom_locale ? [window.__custom_locale] : originalLanguagesDescriptor.get.call(navigator);
                    }
                });
            }
            window.__custom_locale = lang;
        """
        )
        return LocalJsLocale.provides(value)
    }
}
