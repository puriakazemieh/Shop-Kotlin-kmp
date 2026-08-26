plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.kazemieh.shop.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kazemieh.shop"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        
        buildConfigField("String", "BRAND", "\"carmila\"")
        val carmillaApiBase = (project.findProperty("carmillaApiBase") as String?)?.trim().orEmpty()
        buildConfigField("String", "API_BASE_OVERRIDE", "\"$carmillaApiBase\"")
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    flavorDimensions += "brand"
    productFlavors {
        create("carmila") {
            dimension = "brand"
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
    implementation(project(":composeApp"))
    implementation(project(":core:common"))
    implementation(project(":core:designSystem"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    debugImplementation(libs.compose.uiTooling)
}
