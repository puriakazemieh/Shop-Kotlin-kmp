plugins {
    id("carmilla.kmp.library")
    alias(libs.plugins.kotlinxSerialization)
}

// The PR gate invokes the root `test` task; KMP otherwise exposes only `jvmTest`.
tasks.register("test") {
    group = "verification"
    dependsOn("jvmTest")
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
