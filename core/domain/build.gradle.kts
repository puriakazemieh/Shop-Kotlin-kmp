plugins {
    id("carmilla.kmp.library")
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidLibrary {
        namespace = "com.kazemieh.domain"
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization)
                implementation(libs.kotlinx.coroutines.core)
                implementation(project(":core:common"))
                implementation(libs.compose.components.resources)
                implementation(libs.koin.core)
            }
        }
    }
}