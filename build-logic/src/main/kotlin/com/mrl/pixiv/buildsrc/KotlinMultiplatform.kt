package com.mrl.pixiv.buildsrc

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun KotlinMultiplatformAndroidLibraryTarget.configureKotlinMultiplatform() {
    compileSdk {
        version = release(37)
    }
    minSdk = 26

    withDeviceTest {
        instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

internal fun KotlinMultiplatformExtension.commonDependencies() {
    val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
    sourceSets.apply {
        commonMain.dependencies {
            // Lifecycle
            implementation(libs.findBundle("compose-lifecycle").get())
            // Coroutines
            implementation(
                project.dependencies.platform(
                    libs.findLibrary("kotlinx-coroutines-bom").get()
                )
            )
            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
            // Koin
            implementation(libs.findBundle("koin").get())

            // Logger
            implementation(libs.findLibrary("kermit").get())
        }
        androidMain.dependencies {
            implementation(libs.findBundle("androidx").get())
            // Coroutines
            implementation(libs.findLibrary("kotlinx-coroutines-android").get())
        }
    }
    project.dependencies {
        kspAndroid(libs.findLibrary("koin-ksp-compiler").get())
        kspCommonMainMetadata(libs.findLibrary("koin-ksp-compiler").get())
    }
    val kspCommonMetadataTasks =
        project.tasks.matching { it.name == "kspCommonMainKotlinMetadata" }
    project.tasks.configureEach {
        if (name.startsWith("ksp") && name != "kspCommonMainKotlinMetadata") {
            dependsOn(kspCommonMetadataTasks)
        }
    }
}

internal fun KotlinMultiplatformExtension.composeDependencies() {
    val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
    sourceSets.apply {
        commonMain.dependencies {
            // Compose
            implementation(libs.findBundle("compose-baselibs").get())
            implementation(libs.findLibrary("compose-jetbrains-compose-resources").get())
            implementation(libs.findLibrary("compose-jetbrains-ui-tooling-preview").get())
            // KotlinX Collections Immutable
            implementation(libs.findLibrary("kotlinx-collections-immutable").get())
        }
        androidMain.dependencies {
            implementation(libs.findBundle("compose-baselibs-android").get())
        }
    }
}
