plugins {
    id("carmilla.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "com.kazemieh.config.capabilities"
    }

    sourceSets {
        jvmTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
