plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {

    androidLibrary {
        namespace = "com.kazemieh.navigation"
        compileSdk = 36
        minSdk = 24
    }

    val xcfName = "core:navigationKit"

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

                implementation(libs.compose.navigation)
                implementation(project(":feature:auth"))
                implementation(project(":feature:main"))
                implementation(project(":feature:cart"))
                implementation(project(":feature:catalog"))
                implementation(project(":feature:settings"))
                implementation(project(":feature:profile"))
                implementation(project(":feature:orders"))
                implementation(project(":feature:admin:products"))
                implementation(project(":feature:admin:orders"))
                implementation(project(":feature:admin:options"))
                implementation(project(":feature:admin:wallet"))
                implementation(project(":feature:admin:blog"))
                implementation(project(":feature:details"))
                implementation(project(":feature:support"))
                implementation(project(":feature:blog"))
                implementation(project(":feature:academy"))
                implementation(project(":feature:clinic"))
                implementation(project(":core:common"))

                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)


            }
        }

        androidMain {
            dependencies {
            }
        }

        iosMain {
            dependencies {
            }
        }
    }

}