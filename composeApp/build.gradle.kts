import com.mrl.pixiv.buildsrc.configureRemoveKoinMeta

plugins {
    id("pixiv.multiplatform.compose")
}

if (findProperty("applyFirebasePlugins") == "true") {
    pluginManager.apply(libs.plugins.sentry.kmp.get().pluginId)
}

kotlin {
    android {
        namespace = "com.mrl.pixiv.multiplatform"
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":lib_strings"))
                implementation(project(":common:ai"))
                implementation(project(":common:data"))
                implementation(project(":common:datasource-local"))
                implementation(project(":common:network"))
                implementation(project(":common:repository"))
                implementation(project(":common:ui"))
                implementation(project(":common:core"))
                rootDir.resolve("feature").listFiles()?.filter { it.isDirectory }?.forEach {
                    implementation(project(":feature:${it.name}"))
                }
                implementation(libs.bundles.compose.navigation3)
                // Coil3
                implementation(project.dependencies.platform(libs.coil3.bom))
                implementation(libs.bundles.coil3)
                // FileKit
                implementation(libs.filekit.core)
                // MMKV
                implementation(libs.mmkv.kotlin)
            }
        }
        androidMain {
            dependencies {
                // Navigation3
                implementation(libs.bundles.compose.navigation3.android)
            }
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }

    configureRemoveKoinMeta()
}
