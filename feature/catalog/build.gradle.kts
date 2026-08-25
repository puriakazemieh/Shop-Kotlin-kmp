plugins {
    id("carmilla.compose")
}

kotlin {
    androidLibrary {
        namespace = "com.kazemieh.catalog"
        
    }



    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.material.icons.core)
                implementation(libs.androidx.lifecycle.runtimeCompose)

                implementation(libs.koin.compose)
                implementation(libs.compose.navigation)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.image.loader)

                implementation(libs.coil.compose)
                implementation(project(":core:designSystem"))
                implementation(project(":core:domain"))
                implementation(project(":core:common"))


                implementation(libs.compose.material.icons.core)
                implementation(libs.compose.material.icons.extended)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.media3.exoplayer)
                implementation(libs.androidx.media3.ui)
            }
        }
    }
}

