import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)

    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    js {
        browser()
        binaries.executable()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.core.designSystem)

            implementation(project(":core:common"))
            implementation(project(":core:domain"))

            implementation(libs.kotlinx.serialization)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(project(":feature:auth"))
            implementation(project(":feature:profile"))
            implementation(project(":feature:orders"))
            implementation(project(":feature:main"))
            implementation(project(":feature:cart"))
            implementation(project(":feature:catalog"))
            implementation(project(":feature:settings"))
            implementation(project(":feature:details"))
            implementation(project(":feature:admin:products"))
            implementation(project(":feature:admin:orders"))
            implementation(project(":feature:admin:options"))
            implementation(project(":feature:admin:wallet"))
            implementation(project(":feature:admin:blog"))
            implementation(project(":feature:blog"))

            implementation(project(":core:network"))
            implementation(project(":core:data"))
            implementation(project(":core:designSystem"))
            implementation(project(":core:navigation"))


        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }

        jsMain {
            dependencies {
            }
        }
    }
}

android {
    namespace = "com.kazemieh.shop"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.kazemieh.shop"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.kazemieh.shop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.kazemieh.shop"
            packageVersion = "1.0.0"
        }
    }
}
afterEvaluate {
    // ---------- :common ----------
    val commonProject = project(":core:common")
    val composeParentResources =
        File(commonProject.buildDir, "processedResources/jvm/main")
    android.sourceSets["main"].assets.srcDir(composeParentResources.absolutePath)

    tasks.matching {
        (it.name.startsWith("merge") && it.name.endsWith("Assets")) ||
                it.name.contains("Lint", ignoreCase = true)
    }
        .configureEach {
            dependsOn(
                commonProject.tasks.matching {
                    it.name.equals("copyJvmMainComposeResourcesForAndroid", ignoreCase = true) ||
                            it.name.equals("processJvmMainResources", ignoreCase = true) ||
                            it.name.equals("jvmProcessResources", ignoreCase = true)
                }
            )
        }

    // ---------- :core:designsystem ----------
    val designSystemProject = project(":core:designSystem")
    val designSystemParentResources =
        File(designSystemProject.buildDir, "processedResources/jvm/main")
    android.sourceSets["main"].assets.srcDir(designSystemParentResources.absolutePath)

    tasks.matching {
        (it.name.startsWith("merge") && it.name.endsWith("Assets")) ||
                it.name.contains("Lint", ignoreCase = true)
    }
        .configureEach {
            dependsOn(
                designSystemProject.tasks.matching {
                    it.name.equals("copyJvmMainComposeResourcesForAndroid", ignoreCase = true) ||
                            it.name.equals("processJvmMainResources", ignoreCase = true) ||
                            it.name.equals("jvmProcessResources", ignoreCase = true)
                }
            )
        }
}