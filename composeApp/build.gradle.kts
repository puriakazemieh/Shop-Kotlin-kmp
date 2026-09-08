import groovy.json.JsonSlurper
import com.android.build.api.dsl.androidLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("carmilla.compose.application")
}

val generateProductSpecs by tasks.registering {
    val inputFile = rootProject.file("tools/build-config/products.json")
    val outputDir = layout.buildDirectory.dir("generated/source/buildConfig/commonMain/kotlin")
    inputs.file(inputFile)
    outputs.dir(outputDir)

    doLast {
        val outDir = outputDir.get().asFile
        val outFile = File(outDir, "com/kazemieh/shop/GeneratedProductSpecs.kt")
        outFile.parentFile.mkdirs()
        
        val jsonText = inputFile.readText()
        val parsedJson = JsonSlurper().parseText(jsonText) as Map<*, *>
        
        val sb = StringBuilder()
        sb.appendLine("package com.kazemieh.shop")
        sb.appendLine()
        sb.appendLine("import com.kazemieh.config.capabilities.*")
        sb.appendLine("import kotlin.collections.setOf")
        sb.appendLine("import kotlin.collections.mapOf")
        sb.appendLine()
        sb.appendLine("object GeneratedProductSpecs {")
        sb.appendLine("    val specs = mapOf<String, ProductBuildSpec>(")
        
        for ((skuObj, specObj) in parsedJson) {
            val sku = skuObj as String
            val spec = specObj as Map<*, *>
            val tenant = spec["tenant"] as Map<*, *>
            val branding = spec["branding"] as Map<*, *>
            val backend = spec["backendProfile"] as Map<*, *>
            val features = spec["compiledFeatureIds"] as List<*>
            val featuresString = features.joinToString(", ") { "\"$it\"" }
            val authHosts = (backend["allowedAuthHosts"] as List<*>).joinToString(", ") { "\"$it\"" }
            
            sb.appendLine("        \"${sku}\" to ProductBuildSpec(")
            sb.appendLine("            productKind = ProductKind.${spec["productKind"]},")
            sb.appendLine("            sku = \"${spec["sku"]}\",")
            sb.appendLine("            platform = ClientPlatform.${spec["platform"]},")
            sb.appendLine("            tenant = TenantConfig(\"${tenant["id"]}\"),")
            sb.appendLine("            branding = BrandingConfig(\"${branding["id"]}\", \"${branding["displayName"]}\", \"${branding["currency"]}\"),")
            sb.appendLine("            buildIdentity = BuildIdentity(\"com.kazemieh.shop\", \"1.0\", 1),")
            sb.appendLine("            backendProfile = BackendProfile(BackendKind.${backend["kind"]}, \"${backend["apiRoot"]}\", \"${backend["assetRoot"]}\", setOf(${authHosts}), ${backend["contractVersion"]}, \"${backend["manifestPath"]}\"),")
            sb.appendLine("            compiledFeatureIds = setOf(${featuresString})")
            sb.appendLine("        ),")
        }
        sb.appendLine("    )")
        sb.appendLine()
        sb.appendLine("    val localConfigs = mapOf<String, LocalFeatureManifestConfig>(")
        for ((skuObj, specObj) in parsedJson) {
            val sku = skuObj as String
            val spec = specObj as Map<*, *>
            val backend = spec["backendProfile"] as Map<*, *>
            val tenant = spec["tenant"] as Map<*, *>
            val local = spec["localFeatures"] as Map<*, *>
            sb.appendLine("        \"${sku}\" to LocalFeatureManifestConfig(")
            sb.appendLine("            backendKind = BackendKind.${backend["kind"]},")
            sb.appendLine("            tenantId = \"${tenant["id"]}\",")
            sb.appendLine("            contentBlog = ${local["contentBlog"]},")
            sb.appendLine("            commerceCore = ${local["commerceCore"]},")
            sb.appendLine("            commercePhysical = ${local["commercePhysical"]},")
            sb.appendLine("            commerceDigital = ${local["commerceDigital"]}")
            sb.appendLine("        ),")
        }
        sb.appendLine("    )")
        sb.appendLine("}")
        outFile.writeText(sb.toString())
    }
}

kotlin {
    androidLibrary {
        namespace = "com.kazemieh.shop"
    }
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
        }
        commonMain { kotlin.srcDir(generateProductSpecs.map { it.outputs.files.singleFile }) }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.core.designSystem)
            implementation(project(":core:config:capabilities"))

            implementation(project(":core:common"))
            implementation(project(":core:domain"))

            implementation(libs.kotlinx.serialization)
            implementation(libs.ktor.client.core)
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


dependencies {
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


val generatePwaFiles by tasks.registering {
    val inputFile = rootProject.file("tools/build-config/products.json")
    val outputDir = layout.buildDirectory.dir("generated/pwa")
    inputs.file(inputFile)
    outputs.dir(outputDir)

    doLast {
        val outDir = outputDir.get().asFile
        outDir.mkdirs()
        
        val jsonText = inputFile.readText()
        val parsedJson = groovy.json.JsonSlurper().parseText(jsonText) as Map<*, *>
        
        // Generate for WP profile (using carmila)
        val carmilaSpec = parsedJson["carmila"] as Map<*, *>
        val wpBackend = carmilaSpec["backendProfile"] as Map<*, *>
        val wpManifest = File(outDir, "manifest-wp.json")
        wpManifest.writeText("""{
  "id": "com.kazemieh.shop.wp",
  "name": "Carmilla WordPress App",
  "short_name": "CarmillaWP",
  "start_url": "/",
  "scope": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#000000",
  "icons": [
    {
      "src": "/icon-192.png",
      "sizes": "192x192",
      "type": "image/png"
    },
    {
      "src": "/icon-512.png",
      "sizes": "512x512",
      "type": "image/png"
    }
  ]
}""")

        val wpConfig = File(outDir, "app-config-wp.json")
        wpConfig.writeText("""{
  "sku": "carmila",
  "backend": {
    "kind": "WORDPRESS",
    "apiRoot": "${wpBackend["apiRoot"]}",
    "allowedAuthHosts": ["localhost", "127.0.0.1"]
  },
  "features": ["content.blog", "commerce.core", "commerce.physical", "commerce.digital", "wallet"]
}""")

        // Generate for Spring profile (using atris or fake)
        val springManifest = File(outDir, "manifest-spring.json")
        springManifest.writeText("""{
  "id": "com.kazemieh.shop.spring",
  "name": "Carmilla Spring App",
  "short_name": "CarmillaSpring",
  "start_url": "/",
  "scope": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#000000",
  "icons": [
    {
      "src": "/icon-192.png",
      "sizes": "192x192",
      "type": "image/png"
    },
    {
      "src": "/icon-512.png",
      "sizes": "512x512",
      "type": "image/png"
    }
  ]
}""")

        val springConfig = File(outDir, "app-config-spring.json")
        springConfig.writeText("""{
  "sku": "atris",
  "backend": {
    "kind": "SPRING",
    "apiRoot": "http://localhost:8081",
    "allowedAuthHosts": ["localhost", "127.0.0.1"]
  },
  "features": ["content.blog", "commerce.core", "commerce.physical", "commerce.digital", "wallet"]
}""")

        val sw = File(outDir, "sw.js")
        sw.writeText("""
self.addEventListener('install', function(event) {
  event.waitUntil(
    caches.open('carmilla-cache').then(function(cache) {
      return cache.addAll(['/']);
    })
  );
});

self.addEventListener('fetch', function(event) {
  event.respondWith(
    caches.match(event.request).then(function(response) {
      return response || fetch(event.request);
    })
  );
});
""")
    }
}

kotlin.sourceSets.getByName("jsMain") {
    resources.srcDir(generatePwaFiles.map { it.outputs.files.singleFile })
}
