plugins {
    `kotlin-dsl`
}

group = "com.kazemieh.shop.buildlogic"

dependencies {
    implementation("com.android.tools.build:gradle:8.11.2")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.0")
    implementation("org.jetbrains.compose:compose-gradle-plugin:1.10.0")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.3.0")
    implementation("org.jetbrains.compose.hot-reload:org.jetbrains.compose.hot-reload.gradle.plugin:1.0.0")
}


