plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidLibrary {
        namespace = "com.kazemieh.network"
        compileSdk = 36
        minSdk = 24
    }

    val xcfName = "core:networkKit"

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

    js {
        browser()
    }

    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization)
                implementation(libs.koin.core)

                implementation("io.ktor:ktor-client-core:3.3.3")
                implementation("io.ktor:ktor-client-content-negotiation:3.3.3")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.3")
                implementation("io.ktor:ktor-client-logging:3.3.3")
                implementation("io.ktor:ktor-client-auth:3.3.3")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                implementation(project(":core:common"))
                implementation(libs.compose.components.resources)
//                implementation("io.arrow-kt:arrow-core:2.2.2.1")

            }
        }


        androidMain {
            dependencies {
                implementation("io.ktor:ktor-client-android:3.3.3")
                implementation(libs.koin.android)
            }
        }

        iosMain {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.3.3")
            }
        }

        jsMain {
            dependencies {
                implementation("io.ktor:ktor-client-js:3.3.3")
            }
        }

        jvmMain {
            dependencies {
                implementation("io.ktor:ktor-client-cio:3.3.3")
            }
        }

        jvmTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

}
