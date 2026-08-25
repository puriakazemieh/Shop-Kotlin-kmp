import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("carmilla.compose.application")
}

kotlin {
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
            implementation(project(":feature:admin:academy"))
            implementation(project(":feature:admin:clinic"))
            implementation(project(":feature:blog"))
            implementation(project(":feature:support"))
            implementation(project(":feature:academy"))
            implementation(project(":feature:clinic"))
            implementation(project(":feature:psychtest"))
            implementation(project(":feature:admin:psychtest"))
            implementation(project(":feature:comparison"))

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
    

    defaultConfig {
        applicationId = "com.kazemieh.shop"
        
        
        versionCode = 1
        versionName = "1.0"
        // برندِ پیش‌فرض؛ هر فلِیور می‌تواند override کند.
        buildConfigField("String", "BRAND", "\"carmila\"")
        // آدرسِ API را می‌توان هنگامِ بیلد با -PcarmillaApiBase=... تنظیم کرد
        // (مثلاً برای وصل‌کردنِ APK به یک وردپرس/سرورِ مشخص). خالی = پیش‌فرضِ برند.
        val carmillaApiBase = (project.findProperty("carmillaApiBase") as String?)?.trim().orEmpty()
        buildConfigField("String", "API_BASE_OVERRIDE", "\"$carmillaApiBase\"")
    }
    buildFeatures {
        buildConfig = true
    }
    // ---- White-label: هر فلِیور یک اپِ جدا با applicationId و برندِ خودش ----
    flavorDimensions += "brand"
    productFlavors {
        create("carmila") {
            dimension = "brand"
            // برندِ پیش‌فرض؛ از app_name و آیکنِ main استفاده می‌کند.
        }
        create("atris") {
            dimension = "brand"
            applicationIdSuffix = ".atris"
            versionNameSuffix = "-atris"
            buildConfigField("String", "BRAND", "\"atris\"")
        }
        create("chronos") {
            dimension = "brand"
            applicationIdSuffix = ".chronos"
            versionNameSuffix = "-chronos"
            buildConfigField("String", "BRAND", "\"chronos\"")
        }
        create("academy") {
            dimension = "brand"
            applicationIdSuffix = ".academy"
            versionNameSuffix = "-academy"
            buildConfigField("String", "BRAND", "\"academy\"")
        }
        create("psych") {
            dimension = "brand"
            applicationIdSuffix = ".psych"
            versionNameSuffix = "-psych"
            buildConfigField("String", "BRAND", "\"psych\"")
        }
        // اپ ← وردپرس: به‌جای سرورِ Spring Boot، به سایتِ وردپرس (پلاگینِ Carmilla
        // Bridge) وصل می‌شود. آدرسِ سایت در WpBrand.apiBaseUrl تنظیم می‌شود.
        create("wp") {
            dimension = "brand"
            applicationIdSuffix = ".wp"
            versionNameSuffix = "-wp"
            buildConfigField("String", "BRAND", "\"wp\"")
        }
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
