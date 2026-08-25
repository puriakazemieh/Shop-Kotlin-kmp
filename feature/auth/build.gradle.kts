plugins {
    id("carmilla.compose")
}

kotlin {
    androidLibrary {
        namespace = "com.kazemieh.auth"
        
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


                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)

                implementation(project(":core:designSystem"))
                implementation(project(":core:domain"))
                implementation(project(":core:common"))
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

        jsMain {
            dependencies {
            }
        }

        jvmMain {
            dependencies {
            }
        }
    }

}
