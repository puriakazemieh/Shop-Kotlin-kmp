rootProject.name = "kmp-shop"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
//        maven {url = uri("https://maven.myket.ir") }
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
//        maven {url = uri("https://maven.myket.ir") }
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        ivy {
            name = "Node Distributions"
            url = uri("https://nodejs.org/dist")
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
            }
            metadataSources { artifact() }
            content { includeModule("org.nodejs", "node") }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":composeApp")
include(":core:designSystem")
include(":feature:auth")
include(":core:domain")
include(":core:data")
include(":core:network")
include(":core:navigation")
include(":feature:main")
include(":feature:cart")
include(":feature:catalog")
include(":feature:blog")
include(":feature:settings")
include(":feature:admin:products")
include(":feature:admin:orders")
include(":feature:admin:options")
include(":feature:admin:wallet")
include(":feature:admin:blog")
include(":core:common")
include(":feature:profile")
include(":feature:orders")
include(":feature:details")
include(":feature:support")
include(":feature:academy")
include(":feature:clinic")
