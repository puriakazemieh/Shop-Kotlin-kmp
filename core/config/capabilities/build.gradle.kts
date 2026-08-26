plugins {
    id("carmilla.kmp.library")
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidLibrary {
        namespace = "com.kazemieh.config.capabilities"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization)
        }
        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
