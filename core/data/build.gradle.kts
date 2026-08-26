plugins {
    id("carmilla.kmp.library")
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidLibrary {
        namespace = "com.kazemieh.data"
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization)
                implementation(libs.koin.core)
                implementation(project(":core:domain"))
                implementation(project(":core:network"))
                implementation(project(":core:common"))
                implementation(project(":core:config:capabilities"))

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.coroutines)
                implementation(libs.multiplatform.settings.make.observable)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.security.crypto)
            }
        }
    }
}
