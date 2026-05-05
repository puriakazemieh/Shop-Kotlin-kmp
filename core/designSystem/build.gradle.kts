plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
}
compose.resources {
    publicResClass = true
}
kotlin {

    androidLibrary {
        namespace = "com.kazemieh.designsystem"
        compileSdk = 36
        minSdk = 24
        androidResources.enable = true
    }

    val xcfName = "core:designSystemKit"

    iosX64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    jvm()

    js {
        browser()
    }

    sourceSets {

        commonMain {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.kotlinx.serialization)

                implementation(libs.compose.material.icons.core)
                implementation(libs.compose.material.icons.extended)
                implementation(project(":core:common"))
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.core.ktx)
            }
        }

        iosMain {
            dependencies {
            }
        }
    }

}